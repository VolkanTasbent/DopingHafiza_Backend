# 📝 Soru CSV Şablonu Kullanım Kılavuzu

## CSV Formatı

### Başlık Satırı (İlk Satır - Zorunlu)
```csv
soru_metni,sik_a,sik_b,sik_c,sik_d,sik_e,dogru_cevap,zorluk,konular,ders_ad,aciklama,image_url,cozum_videosu_url
```

### Alan Açıklamaları

| Alan | Açıklama | Zorunlu | Örnek |
|------|----------|---------|-------|
| `soru_metni` | Soru metni | ✅ Evet | `"2 + 2 kaçtır?"` |
| `sik_a` | A şıkkı | ⚠️ Opsiyonel | `"3"` veya `""` |
| `sik_b` | B şıkkı | ⚠️ Opsiyonel | `"4"` |
| `sik_c` | C şıkkı | ⚠️ Opsiyonel | `"5"` |
| `sik_d` | D şıkkı | ⚠️ Opsiyonel | `"6"` |
| `sik_e` | E şıkkı | ⚠️ Opsiyonel | `""` (boş olabilir) |
| `dogru_cevap` | Doğru cevap | ✅ Evet | `"A"`, `"B"`, `"C"`, `"D"` veya `"E"` |
| `zorluk` | Soru zorluğu (1-5) | ❌ Opsiyonel | `1`, `2`, `3`, `4`, `5` |
| `konular` | Virgülle ayrılmış konu adları | ✅ Evet | `"Fonksiyonlar,Logaritma"` |
| `ders_ad` | Sorunun ait olduğu ders adı | ✅ Evet | `"Matematik"`, `"Fizik"` vb. |
| `aciklama` | Soru açıklaması | ❌ Opsiyonel | `"Çözüm: (x-1)(x-2)>0"` |
| `image_url` | Soru görseli URL | ❌ Opsiyonel | `"https://example.com/image.jpg"` |
| `cozum_videosu_url` | Çözüm videosu URL | ❌ Opsiyonel | `"https://example.com/video.mp4"` |

---

## 📋 Örnek CSV Dosyası

```csv
soru_metni,sik_a,sik_b,sik_c,sik_d,sik_e,dogru_cevap,zorluk,konular,ders_ad,aciklama,image_url,cozum_videosu_url
"Bir fonksiyon f(x)=2^{x-1} + log₂(x²-3x+2) için tanım kümesi nedir?","x>1","x>2","x<1 veya x>2","x>0 ve x≠1,2","","C",5,"Fonksiyonlar,Logaritma","Matematik","(x-1)(x-2)>0 → x<1 veya x>2.","",""
"x+1/x=3 ise x³+1/x³=?","18","24","27","30","","A",5,"Üslü Sayılar,Cebir","Matematik","Kural: (x+1/x)³ = x³+1/x³+3(x+1/x).","",""
```

---

## ⚠️ Önemli Kurallar

### 1. Tırnak Kullanımı
- **Virgül içeren metinler** mutlaka çift tırnak içinde olmalı:
  ```csv
  "x<1 veya x>2"  ✅ Doğru
  x<1 veya x>2    ❌ Hata (virgül içeriyor)
  ```

- **Tırnak içinde tırnak** kullanmak için çift tırnak:
  ```csv
  """Ali'nin"" sorusu"  → "Ali'nin" sorusu
  ```

### 2. Boş Alanlar
- **E şıkkı yoksa** → `""` (boş string)
- **Zorluk bilinmiyorsa** → Boş bırakılabilir veya `""`
- **Konular yoksa** → `""` (AMA EN AZ BİR KONU GEREKLİ!)

### 3. Doğru Cevap Formatı
- Sadece tek harf: `A`, `B`, `C`, `D`, `E`
- Küçük harf de kabul edilir: `a`, `b`, `c`, `d`, `e` (otomatik büyük harfe çevrilir)
- ❌ `AB`, `1`, `true` → Geçersiz

### 4. Zorluk Değeri
- **1-5** arası tamsayı olmalı
- Geçersiz değerler otomatik `null` yapılır

### 5. Konular
- **Virgülle ayrılmış** konu adları: `"Fonksiyonlar,Logaritma"`
- Konu adları **derse ait olmalı** ve veritabanında mevcut olmalı
- **En az bir konu** gerekli

### 6. Ders Adı
- Veritabanında mevcut bir ders adı olmalı
- Büyük/küçük harf duyarsız (case-insensitive)
- Örnekler: `"Matematik"`, `"Fizik"`, `"Kimya"`, `"Biyoloji"`, `"Türkçe"`, `"Tarih"`

### 7. Kodlama
- CSV dosyası **UTF-8** kodlamasında olmalı
- Türkçe karakterler (ğ, ü, ş, ı, ö, ç) desteklenir
- Özel karakterler (², ³, √, →, vb.) desteklenir

---

## 🎯 Excel'den CSV Oluşturma

1. **Excel'de hazırlayın:**
   - Sütun başlıklarını ekleyin
   - Her satır bir soru

2. **Farklı Kaydet:**
   - `Dosya` → `Farklı Kaydet`
   - Dosya türü: `CSV (Virgülle Ayrılmış) (*.csv)`
   - **Kodlama:** UTF-8 seçin (Windows'ta "CSV UTF-8")

3. **Not:** Excel virgül içeren alanları otomatik tırnak içine alır, ancak manuel kontrol önerilir.

---

## ✅ Doğrulama Kontrol Listesi

CSV yüklemeden önce kontrol edin:

- [ ] İlk satır başlık satırı var mı?
- [ ] Her satırda en az 9 alan var mı?
- [ ] Virgül içeren metinler tırnak içinde mi?
- [ ] `dogru_cevap` A, B, C, D veya E mi?
- [ ] `zorluk` 1-5 arası mı (veya boş)?
- [ ] `soru_metni` boş değil mi?
- [ ] `ders_ad` boş değil mi ve veritabanında mevcut mu?
- [ ] `konular` en az bir konu içeriyor mu ve derse ait mi?
- [ ] Dosya UTF-8 kodlamasında mı?

---

## 🚀 Kullanım

### Backend API:

```bash
POST /api/sorular/import-csv
Content-Type: multipart/form-data
Authorization: Bearer <ADMIN_TOKEN>

file: [soru_csv_sablon.csv]
```

### cURL Örneği:

```bash
curl -X POST http://localhost:8080/api/sorular/import-csv \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -F "file=@soru_csv_sablon.csv"
```

### Response:

```json
{
  "success": true,
  "successCount": 3,
  "errorCount": 0,
  "errors": []
}
```

---

## 🔍 Hata Mesajları

Eğer hata alırsanız, response'daki `errors` dizisinde detaylı bilgi bulunur:

```json
{
  "success": true,
  "successCount": 2,
  "errorCount": 1,
  "errors": [
    "Satır 5: Ders bulunamadı: Fizik",
    "Satır 7: Konu bulunamadı: Trigonometri (Ders: Matematik)"
  ]
}
```

---

## 💡 İpuçları

1. **Toplu Yükleme:** Binlerce soruyu tek seferde yükleyebilirsiniz
2. **Hata Toleransı:** Bir satırda hata olsa bile diğer sorular yüklenir
3. **Konu Kontrolü:** Konu adları tam olarak veritabanındaki gibi olmalı (büyük/küçük harf duyarsız)
4. **Ders Kontrolü:** Ders adı veritabanında mevcut olmalı
5. **Seçenekler:** En az bir seçenek (A, B, C, D veya E) dolu olmalı

---

## 📝 Notlar

- Bu endpoint sadece **ADMIN** kullanıcılar tarafından kullanılabilir
- Yüklenen sorular normal soru tablosuna (`soru`) eklenir
- Seçenekler otomatik olarak `secenek` tablosuna eklenir
- Konular Many-to-Many ilişki ile bağlanır



