# 🛒 Bursa Hal Market — Backend & Android Client

Bursa Büyükşehir Belediyesi'nin açık hal fiyatları verisini kullanan, kullanıcı kaydı, kimlik doğrulama ve sepet yönetimi sunan full-stack market uygulaması.

---

## 📁 Proje Yapısı

```
├── backend/          # Python Flask REST API
│   ├── app.py
│   ├── .env
│   └── requirements.txt
└── android/          # Android Java istemcisi
    └── app/
        └── src/
```

---

## 🔧 Backend — Python Flask

### Gereksinimler

```bash
pip install -r requirements.txt
```

### Ortam Değişkenleri (`.env`)

```env
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_db_password
DB_NAME=your_database
SECRET_KEY=your_jwt_secret
SENDER_EMAIL=your@gmail.com
SMTP_PASSWORD=your_smtp_app_password
GOOGLE_CLIENT_ID=your_google_client_id
```

### Veritabanı Şeması

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    fullname VARCHAR(255),
    mobile VARCHAR(20),
    password VARCHAR(255) NOT NULL
);

CREATE TABLE reset (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code INT NOT NULL,
    expires DATETIME NOT NULL
);

CREATE TABLE baskets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE basket_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    basket_id INT NOT NULL,
    product_id INT NOT NULL,
    FOREIGN KEY (basket_id) REFERENCES baskets(id)
);
```

### Çalıştırma

```bash
python app.py
```

Varsayılan olarak `http://0.0.0.0:5000` adresinde çalışır.

---

## 📡 API Endpointleri

### `POST /activate`
E-posta ile aktivasyon kodu gönderir (kayıt öncesi kullanılır).

**Body:**
```json
{
  "account": "user@example.com",
  "password": "sifre123",
  "confirm": "sifre123"
}
```

---

### `POST /register`
Yeni kullanıcı kaydı. Aktivasyon kodu doğrulaması yapılır.

**Body:**
```json
{
  "account": "user@example.com",
  "fullname": "Ad Soyad",
  "mobile": "05xxxxxxxxx",
  "password": "sifre123",
  "activation_code": 123456
}
```

---

### `POST /login`
E-posta ve şifre ile giriş. JWT token döner.

**Rate Limit:** 5 istek/dakika

**Body:**
```json
{
  "account": "user@example.com",
  "password": "sifre123"
}
```

**Başarılı Yanıt:**
```json
{
  "message": "Giriş başarılı",
  "token": "<JWT>"
}
```

---

### `POST /login-google`
Google ID token ile giriş veya otomatik kayıt.

**Body:**
```json
{
  "token": "<Google ID Token>"
}
```

---

### `PATCH /forgot`
Şifre sıfırlama.

**Rate Limit:** 3 istek/saat

**Body:**
```json
{
  "account": "user@example.com",
  "password": "yeni_sifre"
}
```

---

### `GET /market`
Bursa hal fiyatları listesini döner (kimlik doğrulama gerekmez).

---

### `GET /basket` 🔒
Kullanıcının sepetindeki ürünleri listeler.

**Header:** `Authorization: Bearer <JWT>`

---

### `POST /basket` 🔒
Sepete ürün ekler.

**Header:** `Authorization: Bearer <JWT>`

**Body:**
```json
{ "id": 5 }
```

---

### `DELETE /basket` 🔒
Sepetten ürün çıkarır.

**Header:** `Authorization: Bearer <JWT>`

**Body:**
```json
{ "id": 5 }
```

---

## 📱 Android İstemcisi — Java

### Gereksinimler

- Android Studio
- Min SDK: 24 (Android 7.0)
- Target SDK: 36

### Temel Bağımlılıklar

| Kütüphane | Amaç |
|-----------|------|
| Retrofit 2 + Gson | REST API iletişimi |
| RxJava 2 | Reaktif asenkron işlemler |
| Navigation Component | Fragment yönetimi |
| Google Play Services Auth | OAuth2 akışı |
| RecyclerView | Liste görünümleri |

### Kurulum

1. `google-services.json` dosyanızı `app/` dizinine ekleyin.
2. `build.gradle` içindeki API base URL'ini kendi sunucunuza göre güncelleyin.
3. Projeyi derleyin ve bir emülatör veya gerçek cihazda çalıştırın.

---

## 🔒 Güvenlik

- Şifreler **bcrypt** ile hashlenir.
- Kimlik doğrulama **JWT** (15 dakika geçerlilik) ile sağlanır.
- Giriş endpoint'i **Flask-Limiter** ile brute-force korumalıdır.
- Google Sign-In, sunucu tarafında **google-auth** kütüphanesi ile doğrulanır.
- Aktivasyon kodları 5 dakika sonra otomatik geçersiz kalır.

---

## 🌐 Veri Kaynağı

Ürün verileri uygulama başlangıcında Bursa Büyükşehir Belediyesi açık veri API'sinden çekilir:

```
https://bapi.bursa.bel.tr/apigateway/acikveri/hal-fiyatlari
```

---

## 📜 Lisans

Bu proje eğitim/kişisel kullanım amacıyla geliştirilmiştir.