#!/usr/bin/env python3
"""
Geliştirilmiş PDF → CSV Dönüştürücü
TYT kitapçıkları için optimize edilmiş
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

def find_questions_in_text(text):
    """
    Metinden soruları bul - daha esnek yaklaşım
    """
    questions = []
    
    # Tüm satırları al
    lines = text.split('\n')
    
    current_question = None
    current_options = {}
    collecting_options = False
    
    for i, line in enumerate(lines):
        line = line.strip()
        if not line:
            continue
        
        # Soru numarası ile başlayan satırı bul (örn: "1.", "12.", "40.")
        q_match = re.match(r'^(\d+)\.\s+(.+)$', line)
        if q_match:
            # Önceki soruyu kaydet
            if current_question and current_options:
                questions.append({
                    'number': current_question['num'],
                    'text': current_question['text'],
                    'options': current_options.copy()
                })
            
            # Yeni soru başlat
            current_question = {
                'num': int(q_match.group(1)),
                'text': q_match.group(2)
            }
            current_options = {}
            collecting_options = False
            continue
        
        # Şık bulma (A), B), C), D), E))
        option_match = re.match(r'^([ABCDE])\)\s+(.+)$', line)
        if option_match:
            option_letter = option_match.group(1)
            option_text = option_match.group(2)
            
            # Şık metni çok kısa değilse ekle
            if len(option_text.strip()) > 3:
                current_options[option_letter] = option_text
                collecting_options = True
            continue
        
        # Eğer şık toplama modundaysak ve satır boş değilse, önceki şıka ekle
        if collecting_options and current_options and line:
            # Son eklenen şıka devam et
            last_key = list(current_options.keys())[-1]
            current_options[last_key] += " " + line
    
    # Son soruyu ekle
    if current_question and current_options:
        questions.append({
            'number': current_question['num'],
            'text': current_question['text'],
            'options': current_options.copy()
        })
    
    return questions

def clean_text(text):
    """Metni temizle"""
    if not text:
        return ""
    # Fazla boşlukları temizle
    text = re.sub(r'\s+', ' ', text)
    text = text.strip()
    return text

def questions_to_csv(questions, output_path, ders_ad="Matematik", konular="Genel"):
    """Soruları CSV formatına dönüştür"""
    
    with open(output_path, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        
        # Header
        writer.writerow([
            'soru_metni', 'sik_a', 'sik_b', 'sik_c', 'sik_d', 'sik_e',
            'dogru_cevap', 'zorluk', 'konular', 'ders_ad', 'aciklama',
            'image_url', 'cozum_videosu_url'
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
                ders_ad,
                "",  # Açıklama
                "",  # Image URL
                ""   # Çözüm videosu URL
            ])

def main():
    if len(sys.argv) < 3:
        print("Kullanım: python pdf_to_csv_improved.py <input.pdf> <output.csv> [ders_ad] [konular]")
        print("\nÖrnek:")
        print("  python pdf_to_csv_improved.py yks_tyt_2024_kitapcik_T24kt.pdf output.csv Matematik 'TYT Matematik'")
        sys.exit(1)
    
    input_pdf = sys.argv[1]
    output_csv = sys.argv[2]
    ders_ad = sys.argv[3] if len(sys.argv) > 3 else "Matematik"
    konular = sys.argv[4] if len(sys.argv) > 4 else "Genel"
    
    if not Path(input_pdf).exists():
        print(f"Hata: Dosya bulunamadı: {input_pdf}")
        sys.exit(1)
    
    print(f"PDF okunuyor: {input_pdf}")
    text = extract_text_from_pdf(input_pdf)
    
    if not text:
        print("Hata: PDF'den metin çıkarılamadı")
        sys.exit(1)
    
    print("Sorular parse ediliyor...")
    questions = find_questions_in_text(text)
    
    if not questions:
        print("Uyarı: Hiç soru bulunamadı.")
        print("\nPDF içeriğinin ilk 1000 karakteri:")
        print(text[:1000])
        sys.exit(1)
    
    print(f"{len(questions)} soru bulundu")
    print(f"CSV'ye yazılıyor: {output_csv}")
    
    questions_to_csv(questions, output_csv, ders_ad, konular)
    
    print(f"\n✅ Tamamlandı! {len(questions)} soru CSV'ye yazıldı.")
    print(f"\n⚠️  ÖNEMLİ: CSV dosyasını açıp şunları kontrol edin:")
    print("   1. Doğru cevap sütununu doldurun (A, B, C, D, E)")
    print("   2. Konuları ders bazında düzenleyin")
    print("   3. Soru metinlerini kontrol edin")
    print("   4. Şıkları kontrol edin")

if __name__ == "__main__":
    main()









