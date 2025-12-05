# 📚 PDF Kitapçıklarından Soru Çıkarma Kılavuzu

## 🎯 Yöntem 1: Otomatik Python Scripti (Önerilen)

### Kurulum

```bash
# Python'un yüklü olduğundan emin olun (Python 3.7+)
python3 --version

# Gerekli kütüphaneleri yükleyin
pip install pdfplumber pandas
```

### Kullanım

```bash
# Temel kullanım
python3 pdf_to_csv_converter.py input.pdf output.csv

# Ders ve konu belirtme
python3 pdf_to_csv_converter.py yks_matematik.pdf matematik_sorular.csv Matematik "Fonksiyonlar,Logaritma"

# Örnekler
python3 pdf_to_csv_converter.py tyt_matematik_2024.pdf tyt_mat.csv Matematik "Temel İşlemler,Cebir"
python3 pdf_to_csv_converter.py ayt_fizik_2023.pdf fizik_sorular.csv Fizik "Mekanik,Elektrik"
```

### Script Ne Yapar?

1. ✅ PDF'den metni çıkarır
2. ✅ Soru numaralarını bulur (1., 2., 3. vb.)
3. ✅ Şıkları parse eder (A), B), C), D), E))
4. ✅ CSV formatına dönüştürür
5. ⚠️ **Doğru cevap sütununu BOŞ bırakır** (manuel doldurulmalı)

### Sonrasında Yapılacaklar

1. CSV dosyasını Excel'de açın
2. `dogru_cevap` sütununu doldurun (A, B, C, D, E)
3. `konular` sütununu düzenleyin (her soru için uygun konuları yazın)
4. `zorluk` sütununu doldurun (1-5 arası, opsiyonel)
5. Soru metinlerini ve şıkları kontrol edin
6. Kaydedin ve backend'e yükleyin

---

## 🎯 Yöntem 2: Manuel Copy-Paste (Küçük Miktarlar İçin)

### Adımlar

1. **PDF'i açın** (Adobe Reader, Chrome, vb.)
2. **Soru metnini kopyalayın**
3. **Excel'de şablonu kullanın** (`soru_csv_sablon.csv`)
4. **Her satıra bir soru yapıştırın**
5. **Şıkları ayrı sütunlara yazın**
6. **Doğru cevabı belirtin**

### Excel Şablonu Kullanımı

```csv
soru_metni,sik_a,sik_b,sik_c,sik_d,sik_e,dogru_cevap,zorluk,konular,ders_ad,aciklama,image_url,cozum_videosu_url
"2 + 2 kaçtır?","3","4","5","6","","B",1,"Temel İşlemler","Matematik","","",""
```

---

## 🎯 Yöntem 3: OCR ile Görsel PDF'ler (Gelişmiş)

Eğer PDF görsel formatındaysa (taranmış kitapçık):

### Kurulum

```bash
pip install pytesseract pdf2image pillow
# macOS için:
brew install tesseract
# Linux için:
sudo apt-get install tesseract-ocr
```

### OCR Scripti (Ayrı script gerekir)

Görsel PDF'ler için OCR scripti de hazırlayabilirim. İsterseniz söyleyin!

---

## 📋 PDF Formatlarına Göre İpuçları

### ÖSYM PDF Formatı

- ✅ Genellikle düz metin içerir
- ✅ Soru numaraları: `1.`, `2.`, `12.` formatında
- ✅ Şıklar: `A)`, `B)`, `C)`, `D)`, `E)` formatında
- ✅ Script otomatik parse edebilir

### Taranmış Kitapçıklar

- ⚠️ OCR gerekir
- ⚠️ Format düzensiz olabilir
- ⚠️ Manuel kontrol şart

### Word/Excel'den PDF

- ✅ En kolay parse edilen format
- ✅ Metin kalitesi yüksek
- ✅ Script mükemmel çalışır

---

## 🔧 Sorun Giderme

### Problem: "Hiç soru bulunamadı"

**Çözüm:**
1. PDF formatını kontrol edin
2. PDF'in metin içerdiğinden emin olun (görsel değil)
3. Script'in çıktısına bakın (ilk 500 karakter gösterilir)
4. Regex pattern'leri PDF formatına göre ayarlayın

### Problem: "Şıklar yanlış parse edildi"

**Çözüm:**
1. CSV'yi Excel'de açın
2. Şıkları manuel düzenleyin
3. Veya script'teki regex pattern'leri PDF formatına göre güncelleyin

### Problem: "Türkçe karakterler bozuk"

**Çözüm:**
1. CSV dosyasını UTF-8 kodlamasında kaydedin
2. Excel'de: `Farklı Kaydet` → `CSV UTF-8`
3. Script zaten UTF-8 kullanıyor

---

## 📊 Toplu İşlem İçin

### Birden Fazla PDF'i İşleme

```bash
# Bash script örneği
for pdf in *.pdf; do
    csv_name="${pdf%.pdf}.csv"
    python3 pdf_to_csv_converter.py "$pdf" "$csv_name" Matematik "Genel"
done
```

### Sonra Birleştirme

```bash
# Tüm CSV'leri birleştir (header'ı sadece ilk dosyada tut)
head -1 ilk_dosya.csv > tum_sorular.csv
tail -n +2 *.csv >> tum_sorular.csv
```

---

## ✅ Kontrol Listesi

CSV'yi backend'e yüklemeden önce:

- [ ] Doğru cevap sütunu dolduruldu mu? (A, B, C, D, E)
- [ ] Konular ders bazında doğru mu?
- [ ] Soru metinleri eksiksiz mi?
- [ ] Şıklar doğru parse edilmiş mi?
- [ ] UTF-8 kodlaması doğru mu?
- [ ] Ders adları veritabanında mevcut mu?
- [ ] Konu adları veritabanında mevcut mu?

---

## 🚀 Hızlı Başlangıç

```bash
# 1. Script'i çalıştır
python3 pdf_to_csv_converter.py yks_2024_matematik.pdf sorular.csv Matematik "Fonksiyonlar"

# 2. CSV'yi Excel'de aç
open sorular.csv

# 3. Doğru cevapları doldur

# 4. Backend'e yükle
curl -X POST http://localhost:8080/api/sorular/import-csv \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@sorular.csv"
```

---

## 💡 İpuçları

1. **Küçük test seti:** Önce 10-20 soruyla test edin
2. **Format kontrolü:** PDF formatı farklıysa script'i özelleştirin
3. **Toplu işlem:** Binlerce soru için script'i kullanın
4. **Manuel kontrol:** Her zaman son kontrolü yapın
5. **Yedekleme:** Orijinal PDF'leri saklayın

---

## 📞 Yardım

Script çalışmıyorsa:
1. PDF formatını kontrol edin
2. Python versiyonunu kontrol edin (3.7+)
3. Kütüphanelerin yüklü olduğundan emin olun
4. Hata mesajını okuyun ve düzeltin


