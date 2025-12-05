#!/usr/bin/env python3
"""
PDF'den Deneme Sınavı CSV'sine Dönüştürücü
TYT kitapçıkları için optimize edilmiş - 120 soru çıkarır
"""

import sys
import re
import csv
import pdfplumber
from pathlib import Path

def extract_text_from_pdf(pdf_path):
    """PDF'den metni çıkar"""
    text = ""
    try:
        with pdfplumber.open(pdf_path) as pdf:
            for page in pdf.pages:
                page_text = page.extract_text()
                if page_text:
                    text += page_text + "\n"
    except Exception as e:
        print(f"PDF okuma hatası: {e}")
        return None
    return text

def find_all_questions(text):
    """
    Metinden tüm soruları bul - TYT formatı için optimize
    """
    questions = []
    
    # Tüm satırları al
    lines = text.split('\n')
    
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        
        # Soru numarası ile başlayan satırı bul (örn: "1.", "12.", "40.")
        q_match = re.match(r'^(\d+)\.\s+(.+)$', line)
        if q_match:
            q_num = int(q_match.group(1))
            q_text = q_match.group(2)
            
            # Soru metnini topla (sonraki satırlardan devam edebilir)
            j = i + 1
            while j < len(lines):
                next_line = lines[j].strip()
                # Şık başlangıcı varsa dur
                if re.match(r'^[ABCDE]\)\s+', next_line):
                    break
                # Yeni soru numarası varsa dur
                if re.match(r'^\d+\.\s+', next_line):
                    break
                # Boş satır varsa ve sonraki satır şık değilse dur
                if not next_line and j + 1 < len(lines):
                    if not re.match(r'^[ABCDE]\)\s+', lines[j+1].strip()):
                        break
                # Soru metnine ekle
                if next_line:
                    q_text += " " + next_line
                j += 1
            
            # Şıkları bul
            options = {}
            k = j
            while k < len(lines):
                opt_line = lines[k].strip()
                opt_match = re.match(r'^([ABCDE])\)\s+(.+)$', opt_line)
                if opt_match:
                    opt_letter = opt_match.group(1)
                    opt_text = opt_match.group(2)
                    
                    # Şık metnini topla (sonraki satırlardan devam edebilir)
                    m = k + 1
                    while m < len(lines):
                        next_opt_line = lines[m].strip()
                        # Yeni şık başlangıcı varsa dur
                        if re.match(r'^[ABCDE]\)\s+', next_opt_line):
                            break
                        # Yeni soru numarası varsa dur
                        if re.match(r'^\d+\.\s+', next_opt_line):
                            break
                        # Boş satır varsa ve sonraki satır şık değilse dur
                        if not next_opt_line and m + 1 < len(lines):
                            next_check = lines[m+1].strip()
                            if not re.match(r'^[ABCDE]\)\s+', next_check) and not re.match(r'^\d+\.\s+', next_check):
                                break
                        # Şık metnine ekle
                        if next_opt_line:
                            opt_text += " " + next_opt_line
                        m += 1
                    
                    if len(opt_text.strip()) > 2:  # En az 3 karakter
                        options[opt_letter] = opt_text.strip()
                    k = m
                else:
                    k += 1
                    # Şık toplama bitti mi kontrol et
                    if opt_line and not re.match(r'^[ABCDE]\)', opt_line) and re.match(r'^\d+\.\s+', opt_line):
                        break
            
            # En az bir şık varsa soruyu ekle
            if options:
                questions.append({
                    'number': q_num,
                    'text': q_text.strip(),
                    'options': options
                })
            
            i = k
        else:
            i += 1
    
    return questions

def clean_text(text):
    """Metni temizle"""
    if not text:
        return ""
    # Fazla boşlukları temizle
    text = re.sub(r'\s+', ' ', text)
    text = text.strip()
    return text

def questions_to_deneme_csv(questions, output_path):
    """Soruları deneme sınavı CSV formatına dönüştür"""
    
    with open(output_path, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        
        # Deneme sınavı CSV header
        writer.writerow([
            'soru_metni', 'sik_a', 'sik_b', 'sik_c', 'sik_d', 'sik_e',
            'dogru_cevap', 'zorluk', 'konular', 'aciklama', 'ders_ad'
        ])
        
        # Soruları yaz
        for q in questions:
            question_text = clean_text(q['text'])
            options = q['options']
            
            # Şıkları soru metninden çıkar (eğer varsa)
            for opt in ['A)', 'B)', 'C)', 'D)', 'E)']:
                opt_escaped = opt.replace(')', r'\)')
                question_text = re.sub(
                    rf'{opt_escaped}\s*[^\n]+',
                    '',
                    question_text,
                    flags=re.IGNORECASE
                )
            question_text = clean_text(question_text)
            
            # Şıkları al
            sik_a = clean_text(options.get('A', ''))
            sik_b = clean_text(options.get('B', ''))
            sik_c = clean_text(options.get('C', ''))
            sik_d = clean_text(options.get('D', ''))
            sik_e = clean_text(options.get('E', ''))
            
            # En az bir şık olmalı
            if not any([sik_a, sik_b, sik_c, sik_d, sik_e]):
                continue
            
            # Doğru cevap bilinmiyor, manuel doldurulmalı
            dogru_cevap = ""
            
            # Ders adını soru numarasına göre tahmin et (TYT: 1-40 Türkçe, 41-80 Matematik, 81-100 Fen, 101-120 Sosyal)
            q_num = q['number']
            if q_num <= 40:
                ders_ad = "Türkçe"
                konular = "TYT Türkçe"
            elif q_num <= 80:
                ders_ad = "Matematik"
                konular = "TYT Matematik"
            elif q_num <= 100:
                ders_ad = "Fen Bilimleri"
                konular = "TYT Fen Bilimleri"
            else:
                ders_ad = "Sosyal Bilimler"
                konular = "TYT Sosyal Bilimler"
            
            writer.writerow([
                question_text,
                sik_a,
                sik_b,
                sik_c,
                sik_d,
                sik_e,
                dogru_cevap,
                "",  # Zorluk - manuel doldurulacak
                konular,
                "",  # Açıklama
                ders_ad
            ])

def main():
    if len(sys.argv) < 3:
        print("Kullanım: python pdf_to_deneme_sinavi.py <input.pdf> <output.csv>")
        print("\nÖrnek:")
        print("  python pdf_to_deneme_sinavi.py yks_tyt_2024_kitapcik_T24kt.pdf tyt_2024_deneme.csv")
        sys.exit(1)
    
    input_pdf = sys.argv[1]
    output_csv = sys.argv[2]
    
    if not Path(input_pdf).exists():
        print(f"Hata: Dosya bulunamadı: {input_pdf}")
        sys.exit(1)
    
    print(f"PDF okunuyor: {input_pdf}")
    text = extract_text_from_pdf(input_pdf)
    
    if not text:
        print("Hata: PDF'den metin çıkarılamadı")
        sys.exit(1)
    
    print("Sorular parse ediliyor...")
    questions = find_all_questions(text)
    
    if not questions:
        print("Uyarı: Hiç soru bulunamadı.")
        sys.exit(1)
    
    # Soru numaralarına göre sırala
    questions.sort(key=lambda x: x['number'])
    
    print(f"{len(questions)} soru bulundu")
    
    if len(questions) < 100:
        print(f"⚠️  Uyarı: Beklenen 120 soru yerine {len(questions)} soru bulundu.")
        print("   PDF formatı farklı olabilir veya bazı sorular parse edilememiş olabilir.")
    
    print(f"CSV'ye yazılıyor: {output_csv}")
    
    questions_to_deneme_csv(questions, output_csv)
    
    print(f"\n✅ Tamamlandı! {len(questions)} soru deneme sınavı CSV formatına yazıldı.")
    print(f"\n⚠️  ÖNEMLİ: CSV dosyasını açıp şunları kontrol edin:")
    print("   1. Doğru cevap sütununu doldurun (A, B, C, D, E)")
    print("   2. Konuları ders bazında düzenleyin")
    print("   3. Soru metinlerini ve şıkları kontrol edin")
    print(f"\n📝 Sonraki adım:")
    print("   1. Deneme sınavı oluşturun: POST /api/deneme-sinavi")
    print("   2. CSV'yi import edin: POST /api/deneme-sinavlari/{denemeId}/import-csv")

if __name__ == "__main__":
    main()


