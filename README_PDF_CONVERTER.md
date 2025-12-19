# 📄 PDF'den Soru Çıkarma - Hızlı Başlangıç

## 🚀 3 Adımda Başlayın

### 1. Kütüphaneleri Yükleyin

```bash
pip install -r pdf_converter_requirements.txt
```

veya

```bash
pip install pdfplumber pandas
```

### 2. PDF'inizi Hazırlayın

- ÖSYM'den indirdiğiniz PDF kitapçığı
- Veya herhangi bir YKS soru bankası PDF'i

### 3. Script'i Çalıştırın

```bash
python3 pdf_to_csv_converter.py your_pdf.pdf output.csv Matematik "Fonksiyonlar,Logaritma"
```

**Parametreler:**
- `your_pdf.pdf` → PDF dosyanızın adı
- `output.csv` → Çıktı CSV dosyası
- `Matematik` → Ders adı (opsiyonel, varsayılan: Matematik)
- `"Fonksiyonlar,Logaritma"` → Konular (opsiyonel, varsayılan: Genel)

## 📝 Örnekler

```bash
# TYT Matematik
python3 pdf_to_csv_converter.py tyt_mat_2024.pdf tyt_mat.csv Matematik "Temel İşlemler"

# AYT Fizik
python3 pdf_to_csv_converter.py ayt_fizik_2023.pdf fizik.csv Fizik "Mekanik,Elektrik"

# Sadece PDF ve CSV (varsayılan değerlerle)
python3 pdf_to_csv_converter.py sorular.pdf cikti.csv
```

## ⚠️ ÖNEMLİ: CSV'yi Kontrol Edin

Script çalıştıktan sonra:

1. **CSV'yi Excel'de açın**
2. **`dogru_cevap` sütununu doldurun** (A, B, C, D, E)
3. **`konular` sütununu düzenleyin** (her soru için uygun konuları yazın)
4. **Soru metinlerini ve şıkları kontrol edin**
5. **Kaydedin ve backend'e yükleyin**

## 🔧 Sorun mu Var?

Detaylı kılavuz için: `PDF_DEN_SORU_CIKARMA_KILAVUZU.md`

## 📊 Sonuç

Script çalıştıktan sonra:
- ✅ CSV dosyası hazır
- ⚠️ Doğru cevapları manuel doldurun
- ✅ Backend'e yüklemeye hazır!










