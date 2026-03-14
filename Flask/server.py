from flask import Flask, request, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from functools import wraps
from random import randint
import os
import requests as req
from dotenv import load_dotenv, find_dotenv
import smtplib, ssl
import bcrypt
import logging
from google.oauth2 import id_token
from google.auth.transport import requests
import jwt
import datetime
from mysql.connector import pooling, Error

dotenv_path = find_dotenv()
load_dotenv(dotenv_path)

content = req.get("https://bapi.bursa.bel.tr/apigateway/acikveri/hal-fiyatlari")
data = content.json()

SMTP_SERVER = "smtp.gmail.com"
PORT = 587
SENDER_EMAIL = os.getenv("SENDER_EMAIL")
PASSWORD = os.getenv("SMTP_PASSWORD")

connection_pool = pooling.MySQLConnectionPool(
    pool_name = "mypool",
    pool_size = 10,
    host = os.getenv("DB_HOST"),
    user = os.getenv("DB_USER"),
    password = os.getenv("DB_PASSWORD"),
    database = os.getenv("DB_NAME")
)

app = Flask(__name__)
app.config["SECRET_KEY"] = os.getenv("SECRET_KEY")

limiter = Limiter(
    get_remote_address,
    app=app,
    default_limits=["50 per hour"],
)

def activate_code_send(RECEIVER_EMAIL, ACTIVATE_CODE):
    MESSAGE = f"Subject: Hi {RECEIVER_EMAIL}\n\nAccount activation code: {ACTIVATE_CODE}"
    context = ssl.create_default_context()
    try:
        with smtplib.SMTP(SMTP_SERVER, PORT) as server:
            server.starttls(context=context)
            server.login(SENDER_EMAIL, PASSWORD)
            server.sendmail(SENDER_EMAIL, RECEIVER_EMAIL, MESSAGE.encode("utf-8"))
        return True
    except Exception as e:
        logging.error(str(e))
        return False

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.headers.get("Authorization")
        if not auth:
            return jsonify({"message": "Token eksik"}), 401
        try:
            token = auth.split(" ")[1]
            token_data = jwt.decode(token, app.config["SECRET_KEY"], algorithms=["HS256"])
            email = token_data["email"]
            mydb = connection_pool.get_connection()
            mycursor = mydb.cursor()
            mycursor.execute("SELECT id, email FROM users WHERE email = %s", (email,))
            current_user = mycursor.fetchone()
            mycursor.close()
            mydb.close()
            if not current_user:
                return jsonify({"message": "Kullanıcı bulunamadı"}), 401
        except jwt.ExpiredSignatureError:
            return jsonify({"message": "Token süresi dolmuş"}), 401
        except jwt.InvalidTokenError:
            return jsonify({"message": "Geçersiz token"}), 401
        return f(current_user, *args, **kwargs)
    return decorated

@app.route("/login", methods = ["POST"])
@limiter.limit("5 per minute")
def login():
    mycursor = None
    mydb = None
    try:
        content = request.get_json()
        if not content:
            return jsonify({"message": "Geçersiz JSON"}), 400
        else:
            email = content.get("account")
            password = content.get("password")
            mydb = connection_pool.get_connection()
            mycursor = mydb.cursor()
            sql = "SELECT password FROM users WHERE email = %s"
            mycursor.execute(sql, (email,))
            result = mycursor.fetchone()
            if not result:
                return jsonify({"message": "Kullanıcı bulunamadı"}), 404
            hashed_password = result[0]
            if bcrypt.checkpw(password.encode("utf-8"), hashed_password.encode("utf-8")):
                token = jwt.encode(
                    {
                        "email": email,
                        "exp": datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(minutes=15)
                    },
                    app.config["SECRET_KEY"],
                    algorithm="HS256"
                )
                return jsonify({"message": "Giriş başarılı", "token": token}), 200
            else:
                return jsonify({"message": "Hatalı şifre"}), 401
    except Error as db_error:
        logging.error(str(db_error))
        return jsonify({"message": "Veritabanı hatası"}), 500
    except Exception as e:
        logging.error(str(e))
        return jsonify({"message": "Sunucu hatası"}), 500
    finally:
        if mycursor:
            mycursor.close()
        if mydb:
            mydb.close()

@app.route("/register", methods = ["POST"])
def register():
    mycursor = None
    mydb = None
    try:
        content = request.get_json()
        if not content:
            return jsonify({"message": "Geçersiz JSON"}), 400
        else:
            email = content.get("account")
            fullname = content.get("fullname")
            mobile = content.get("mobile")
            password = content.get("password")
            activation_code = content.get("activation_code")
            now = datetime.datetime.now()
            mydb = connection_pool.get_connection()
            mycursor = mydb.cursor()

            account_control_sql = "SELECT email, mobile FROM users WHERE email = %s OR mobile = %s"
            mycursor.execute(account_control_sql, (email, mobile))
            result = mycursor.fetchone()
            if result:
                db_email, db_mobile = result
                if db_email == email:
                    return jsonify({"message": "Email zaten kayıtlı"}), 400
                elif db_mobile == mobile:
                    return jsonify({"message": "Telefon numarası kayıtlı"}), 400
            
            delete_code_sql = "DELETE FROM reset WHERE email = %s AND code = %s AND expires > %s"
            mycursor.execute(delete_code_sql, (email, activation_code, now))
            if mycursor.rowcount == 0:
                return jsonify({"message": "Aktivasyon kodu geçersiz veya süresi dolmuş"}), 400

            hashed_password = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
            users_sql = "INSERT INTO users (email, fullname, mobile, password) VALUES (%s, %s, %s, %s)" # "INSERT INTO users (email, fullname, mobile, password) SELECT %s, %s, %s, %s WHERE EXISTS (SELECT 1 FROM reset WHERE email = %s AND code = %s AND expires > %s)"
            mycursor.execute(users_sql, (email, fullname, mobile, hashed_password)) # , email, activation_code, now))
            baskets_sql = "INSERT INTO baskets (user_id) SELECT u.id FROM users u WHERE u.email = %s" # lastrowid
            mycursor.execute(baskets_sql, (email,))
            mydb.commit()
            return jsonify({"message": "Kayıt başarılı"}), 200
            
    except Error as db_error:
        if mydb:
            mydb.rollback()
        logging.error(str(db_error))
        return jsonify({"message": "Veritabanı hatası"}), 500
    except Exception as e:
        logging.error(str(e))
        return jsonify({"message": "Sunucu hatası"}), 500
    finally:
        if mycursor:
            mycursor.close()
        if mydb:
            mydb.close()

@app.route("/activate", methods = ["POST"])
def activate(): # Önce varsa emailin delete tablosundaki verileri silinecek. Daha sonra kod gönderilecek. Önceki veri temizlenmiş olacak.
    mycursor = None
    mydb = None
    try:
        content = request.get_json()
        if not content:
            return jsonify({"message": "Geçersiz JSON"}), 400
        else:
            email = content.get("account")
            password = content.get("password")
            confirm = content.get("confirm")
            if password != confirm:
                return jsonify({"message": "Girilen şifreler uyuşmuyor"}), 401
            mydb = connection_pool.get_connection()
            mycursor = mydb.cursor()
            fresh_sql = "DELETE FROM reset WHERE email = %s"
            mycursor.execute(fresh_sql, (email,))
            activation_code = randint(100000,999999)
            expires = datetime.datetime.now() + datetime.timedelta(minutes=5)
            full_sql = "INSERT INTO reset (email, code, expires) VALUES (%s, %s, %s)" # SELECT u.email, %s, %s FROM users u WHERE u.email = %s"
            mycursor.execute(full_sql, (email, activation_code, expires)) # (activation_code, expires, email))
            mydb.commit()
            if not activate_code_send(email, activation_code): # if activate_code_send(email, email, activation_code) == False:
                return jsonify({"message": "Mail gönderilemedi"}), 500
            return jsonify({"message": "Aktivasyon kodu gönderildi"}), 200 # else
    except Error as db_error:
        if mydb:
            mydb.rollback()
        logging.error(str(db_error))
        return jsonify({"message": "Veritabanı hatası"}), 500
    except Exception as e:
        logging.error(str(e))
        return jsonify({"message": "Sunucu hatası"}), 500
    finally:
        if mycursor:
            mycursor.close()
        if mydb:
            mydb.close()

@app.route("/forgot", methods = ["PATCH"])
@limiter.limit("3 per hour")
def forgot():
    mycursor = None
    mydb = None
    try:
        content = request.get_json()
        if not content:
            return jsonify({"message": "Geçersiz JSON"}), 400
        else:
            email = content.get("account")
            password = content.get("password")
            mydb = connection_pool.get_connection()
            mycursor = mydb.cursor()
            account_sql = "SELECT email FROM users WHERE email = %s"
            mycursor.execute(account_sql, (email,))
            result = mycursor.fetchone()
            if result:
                db_email = result[0] # email
                password_sql = "UPDATE users SET password = %s WHERE email = %s"
                hashed_password = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
                mycursor.execute(password_sql, (hashed_password, db_email))
                mydb.commit()
                return jsonify({"message": "Şifre değişimi başarılı"}), 200
            return jsonify({"message": "Kullanıcı bulunamadı"}), 401
    except Error as db_error:
        logging.error(str(db_error))
        return jsonify({"message": "Veritabanı hatası"}), 500
    except Exception as e:
        logging.error(str(e))
        return jsonify({"message": "Sunucu hatası"}), 500
    finally:
        if mycursor:
            mycursor.close()
        if mydb:
            mydb.close()

@app.route("/market", methods = ["GET"])
def market():
    market = [
        {
            "urun_ad": data[i]["urun_ad"],
            "KategoriIsim": data[i]["KategoriIsim"],
            "max": data[i]["max"],
            "min": data[i]["min"],
            "br": data[i]["br"],
            "id": data[i]["id"]
        }
        for i in range(len(data))
    ]
    return jsonify({"market": market}), 200

@app.route("/basket", methods = ["GET", "POST", "DELETE"])
@token_required
def basket(current_user):
    mycursor = None
    mydb = None
    try:
        if request.method == "GET":
            email = current_user[1]
            mydb = connection_pool.get_connection()
            mycursor = mydb.cursor()
            # basket'teki ürünlerin id'leri gerekiyor
            products_sql = "SELECT bi.product_id FROM basket_items bi INNER JOIN baskets b ON bi.basket_id = b.id INNER JOIN users u ON b.user_id = u.id WHERE u.email = %s"
            mycursor.execute(products_sql, (email,))
            result = mycursor.fetchall()
            if len(result) != 0:
                products = [
                    {
                        "urun_ad": data[product_id[0]]["urun_ad"],
                        "KategoriIsim": data[product_id[0]]["KategoriIsim"],
                        "max": data[product_id[0]]["max"],
                        "min": data[product_id[0]]["min"],
                        "br": data[product_id[0]]["br"],
                        "id": data[product_id[0]]["id"]
                    } for product_id in result
                ]
                return jsonify({"market": products}), 200
            return jsonify({"market": []}), 200
        else:
            content = request.get_json()
            if not content:
                return jsonify({"message": "Geçersiz JSON"}), 400
            else:
                id = int(content.get("id", 0)) - 1 # ürünün id'si
                email = current_user[1]
                mydb = connection_pool.get_connection()
                mycursor = mydb.cursor()
                if request.method == "POST":
                    insert_product_sql = "INSERT INTO basket_items (basket_id, product_id) SELECT b.id, %s FROM baskets b INNER JOIN users u ON u.id = b.user_id WHERE u.email = %s AND NOT EXISTS (SELECT 1 FROM basket_items bi WHERE bi.basket_id = b.id AND bi.product_id = %s)"
                    mycursor.execute(insert_product_sql, (id, email, id))
                    mydb.commit()
                    return jsonify({"message": f"{data[id]['urun_ad']} sepete eklendi"}), 200
                elif request.method == "DELETE":
                    delete_sql = "DELETE bi FROM basket_items bi INNER JOIN baskets b ON bi.basket_id = b.id INNER JOIN users u ON b.user_id = u.id WHERE u.email = %s AND bi.product_id = %s"
                    mycursor.execute(delete_sql, (email, id))
                    mydb.commit()
                    return jsonify({"message": f"{data[id]['urun_ad']} sepetten çıkarıldı"}), 200
    except Error as db_error:
        logging.error(str(db_error))
        return jsonify({"message": "Veritabanı hatası"}), 500
    except Exception as e:
        logging.error(str(e))
        return jsonify({"message": "Sunucu hatası"}), 500
    finally:
        if mycursor:
            mycursor.close()
        if mydb:
            mydb.close()

@app.route("/login-google", methods = ["POST"])
def login_google():
    mycursor = None
    mydb = None
    try:
        content = request.get_json()
        if not content:
            return jsonify({"message": "Geçersiz JSON"}), 400
        else:
            idtoken = content.get("token")
            mydb = connection_pool.get_connection()
            mycursor = mydb.cursor()
            requestx = requests.Request()
            client_id = os.getenv("GOOGLE_CLIENT_ID")
            id_info = id_token.verify_oauth2_token(idtoken, requestx, client_id)
            userid = id_info['sub'] # id
            
            insert_sql = "INSERT INTO users (email, fullname, mobile, password) SELECT %s, %s, %s, %s WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = %s)"
            hashed_password = bcrypt.hashpw(userid.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")
            mycursor.execute(insert_sql, (id_info["email"], id_info["name"], "0", hashed_password, id_info["email"]))
            basket_sql = "INSERT INTO baskets (user_id) SELECT u.id FROM users u WHERE u.email = %s AND NOT EXISTS (SELECT 1 FROM baskets b WHERE b.user_id = u.id)"
            mycursor.execute(basket_sql, (id_info["email"],))
            mydb.commit()
            if bcrypt.checkpw(userid.encode("utf-8"), hashed_password.encode("utf-8")):
                token = jwt.encode(
                    {
                        "email": id_info["email"],
                        "exp": datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(minutes=15)
                    },
                    app.config["SECRET_KEY"],
                    algorithm="HS256"
                )
                return jsonify({"message": "Giriş başarılı", "token": token}), 200
            else:
                return jsonify({"message": "Hatalı şifre"}), 401
    except Error as db_error:
        logging.error(str(db_error))
        return jsonify({"message": "Veritabanı hatası"}), 500
    except Exception as e:
        logging.error(str(e))
        return jsonify({"message": "Sunucu hatası"}), 500
    finally:
        if mycursor:
            mycursor.close()
        if mydb:
            mydb.close()

if __name__ == "__main__":
    app.run(host="0.0.0.0")