#!/usr/bin/env python3
"""
YKS PDF Kitapçıklarından Soruları CSV'ye Dönüştürme Scripti

Kullanım:
    python pdf_to_csv_converter.py input.pdf output.csv

Gereksinimler:
    pip install pdfplumber pandas
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

def parse_question(text, question_num):
    """
    Metinden soru bilgilerini parse et
    YKS formatına göre soru numarası ve şıkları bulur
    """
    # Soru numarasını bul (örn: "1.", "2.", "12.") - satır başında olmalı
    # Önce soru numarasının geçtiği yerleri bul
    next_num = str(int(question_num) + 1)
    pattern = rf"^{question_num}\.\s*(.+?)(?=^{next_num}\.|$)"
    match = re.search(pattern, text, re.DOTALL | re.MULTILINE | re.IGNORECASE)
    
    if not match:
        # Alternatif: satır içinde soru numarası
        pattern = rf"{question_num}\.\s*(.+?)(?=\d+\.|$)"
        match = re.search(pattern, text, re.DOTALL | re.IGNORECASE)
    
    if not match:
        return None
    
    question_text = match.group(1).strip()
    
    # Şıkları bul (A), B), C), D), E))
    options = {}
    option_patterns = {
        'A': r'A\)\s*(.+?)(?=[BCDE]\)|$)',
        'B': r'B\)\s*(.+?)(?=[CDE]\)|$)',
        'C': r'C\)\s*(.+?)(?=[DE]\)|$)',
        'D': r'D\)\s*(.+?)(?=E\)|$)',
        'E': r'E\)\s*(.+?)(?=\d+\.|$)'
    }
    
    for option, pattern in option_patterns.items():
        opt_match = re.search(pattern, question_text, re.DOTALL | re.IGNORECASE)
        if opt_match:
            opt_text = opt_match.group(1).strip()
            # Şık metninin çok kısa olmaması lazım (en az 3 karakter)
            if len(opt_text) > 2:
                options[option] = opt_text
    
    # En az bir şık bulunmalı
    if not options:
        return None
    
    return {
        'question': question_text,
        'options': options
    }

def extract_all_questions(text):
    """Tüm soruları çıkar"""
    questions = []
    
    # Soru numaralarını bul (1. ile başlayan satırlar veya satır içinde)
    # Önce satır başında olanları bul
    question_numbers = re.findall(r'^(\d+)\.', text, re.MULTILINE)
    
    # Eğer yeterli soru bulunamazsa, satır içindeki numaraları da dene
    if len(question_numbers) < 10:
        question_numbers = re.findall(r'\b(\d+)\.\s', text)
        # Tekrarları kaldır ve sırala
        question_numbers = sorted(set(int(x) for x in question_numbers if x.isdigit()), key=int)
        question_numbers = [str(x) for x in question_numbers]
    
    # İlk 200 soruya kadar kontrol et (TYT'de genelde 40 soru var ama farklı dersler olabilir)
    max_questions = min(200, len(question_numbers))
    
    for i in range(max_questions):
        if i >= len(question_numbers):
            break
        q_num = question_numbers[i]
        parsed = parse_question(text, q_num)
        if parsed and parsed['options']:  # En az bir şık olmalı
            questions.append({
                'number': i + 1,
                'text': parsed['question'],
                'options': parsed['options']
            })
    
    return questions

def clean_text(text):
    """Metni temizle"""
    # Fazla boşlukları temizle
    text = re.sub(r'\s+', ' ', text)
    # Özel karakterleri koru ama fazla boşlukları kaldır
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
            # Soru metnini temizle (şıkları çıkar)
            question_text = q['text']
            options = q['options']
            
            # Şıkları soru metninden çıkar
            for opt in ['A)', 'B)', 'C)', 'D)', 'E)']:
                # Parantez karakterini escape et
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
            
            # Doğru cevap bilinmiyor, manuel doldurulmalı
            dogru_cevap = ""  # Manuel doldurulacak
            
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
        print("Kullanım: python pdf_to_csv_converter.py <input.pdf> <output.csv> [ders_ad] [konular]")
        print("\nÖrnek:")
        print("  python pdf_to_csv_converter.py yks_matematik.pdf output.csv Matematik 'Fonksiyonlar,Logaritma'")
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
    questions = extract_all_questions(text)
    
    if not questions:
        print("Uyarı: Hiç soru bulunamadı. PDF formatı farklı olabilir.")
        print("\nPDF içeriğinin ilk 500 karakteri:")
        print(text[:500])
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

