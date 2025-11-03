# 📝 Deneme Sınavı CSV Şablonu Kullanım Kılavuzu

## CSV Formatı

### Başlık Satırı (İlk Satır - Zorunlu)
```csv
soru_metni,sik_a,sik_b,sik_c,sik_d,sik_e,dogru_cevap,zorluk,konular,aciklama,ders_ad
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
| `konular` | Virgülle ayrılmış konu adları | ❌ Opsiyonel | `"Fonksiyonlar,Logaritma"` |
| `aciklama` | Soru açıklaması | ❌ Opsiyonel | `"Çözüm: (x-1)(x-2)>0"` |
| `ders_ad` | Sorunun ait olduğu ders adı | ❌ Opsiyonel | `"Matematik"`, `"Fizik"` vb. |

---

## 📋 Örnek CSV Dosyası

```csv
soru_metni,sik_a,sik_b,sik_c,sik_d,sik_e,dogru_cevap,zorluk,konular,aciklama
"Bir fonksiyon f(x)=2^{x-1} + log₂(x²-3x+2) için tanım kümesi nedir?","x>1","x>2","x<1 veya x>2","x>0 ve x≠1,2","","C",5,"Fonksiyonlar,Logaritma","(x-1)(x-2)>0 → x<1 veya x>2."
"x+1/x=3 ise x³+1/x³=?","18","24","27","30","","A",5,"Üslü Sayılar,Cebir","Kural: (x+1/x)³ = x³+1/x³+3(x+1/x)."
"Bir geometrik dizide a₁=2,a₅=162 ise r kaçtır?","2","3","4","5","","B",4,"Diziler,Geometrik Dizi","a₅=a₁·r⁴ → 162=2r⁴ → r=3."
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
- **Konular yoksa** → `""`

### 3. Doğru Cevap Formatı
- Sadece tek harf: `A`, `B`, `C`, `D`, `E`
- Küçük harf de kabul edilir: `a`, `b`, `c`, `d`, `e` (otomatik büyük harfe çevrilir)
- ❌ `AB`, `1`, `true` → Geçersiz

### 4. Zorluk Değeri
- **1-5** arası tamsayı olmalı
- Geçersiz değerler otomatik `null` yapılır

### 5. Kodlama
- CSV dosyası **UTF-8** kodlamasında olmalı
- Türkçe karakterler (ğ, ü, ş, ı, ö, ç) desteklenir
- Özel karakterler (², ³, √, →, vb.) desteklenir

---

## 📝 Minimum Şablon (Basit Kullanım)

```csv
soru_metni,sik_a,sik_b,sik_c,sik_d,sik_e,dogru_cevap,zorluk,konular,aciklama,ders_ad
"2 + 2 kaçtır?","3","4","5","6","","B",1,"Temel İşlemler","","Matematik"
"3 x 5 kaçtır?","12","15","18","20","","B",2,"Çarpma","","Matematik"
```

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
- [ ] Her satırda tam 10 alan var mı?
- [ ] Virgül içeren metinler tırnak içinde mi?
- [ ] `dogru_cevap` A, B, C, D veya E mi?
- [ ] `zorluk` 1-5 arası mı (veya boş)?
- [ ] `soru_metni` boş değil mi?
- [ ] Dosya UTF-8 kodlamasında mı?

---

## 🚀 Kullanım

### Backend API:

```bash
POST /api/deneme-sinavlari/{denemeId}/import-csv
Content-Type: multipart/form-data

file: [deneme_sinavi.csv]
```

### Response:

```json
{
  "success": true,
  "denemeId": 1,
  "successCount": 35,
  "errorCount": 2,
  "errors": [
    "Satır 5: Geçersiz doğru cevap: F",
    "Satır 12: Soru metni boş"
  ]
}
```

---

## 💡 İpuçları

1. **Excel'de hazırlayın, CSV olarak kaydedin**
2. **Virgül içeren metinleri kontrol edin**
3. **E şıkkı yoksa boş string (`""`) kullanın**
4. **UTF-8 kodlama kullanın** (Türkçe karakterler için)
5. **Hata mesajlarını kontrol edin** - hangi satırlarda sorun var gösterilir

---

## 📄 Örnek Dosya

Örnek CSV dosyası: `deneme_sinavi_sablon.csv` (proje kök dizininde)

