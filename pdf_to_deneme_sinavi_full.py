#!/usr/bin/env python3
"""
PDF'den Tüm Testleri Çıkarıp Deneme Sınavı CSV'sine Dönüştürücü
TYT kitapçığı için - Tüm 120 soruyu çıkarır
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

def find_test_sections(text):
    """Test bölümlerini bul"""
    sections = {}
    lines = text.split('\n')
    
    current_test = None
    for i, line in enumerate(lines):
        if 'TÜRKÇE TESTİ' in line:
            sections['Türkçe'] = i
            current_test = 'Türkçe'
        elif 'SOSYAL BİLİMLER TESTİ' in line:
            sections['Sosyal'] = i
            current_test = 'Sosyal'
        elif 'TEMEL MATEMATİK TESTİ' in line or 'MATEMATİK TESTİ' in line:
            sections['Matematik'] = i
            current_test = 'Matematik'
        elif 'FEN BİLİMLERİ TESTİ' in line:
            sections['Fen'] = i
            current_test = 'Fen'
    
    return sections

def extract_questions_from_section(text_lines, start_idx, end_idx=None, test_name=""):
    """Belirli bir bölümden soruları çıkar"""
    questions = []
    
    i = start_idx
    while i < len(text_lines) and (end_idx is None or i < end_idx):
        line = text_lines[i].strip()
        
        # Soru numarası ile başlayan satırı bul
        q_match = re.match(r'^(\d+)\.\s+(.+)$', line)
        if q_match:
            q_num = int(q_match.group(1))
            q_text = q_match.group(2)
            
            # Soru metnini topla
            j = i + 1
            while j < len(text_lines):
                next_line = text_lines[j].strip()
                if re.match(r'^[ABCDE]\)\s+', next_line):
                    break
                if re.match(r'^\d+\.\s+', next_line):
                    break
                if next_line:
                    q_text += " " + next_line
                j += 1
            
            # Şıkları bul
            options = {}
            k = j
            while k < len(text_lines):
                opt_line = text_lines[k].strip()
                opt_match = re.match(r'^([ABCDE])\)\s+(.+)$', opt_line)
                if opt_match:
                    opt_letter = opt_match.group(1)
                    opt_text = opt_match.group(2)
                    
                    # Şık metnini topla
                    m = k + 1
                    while m < len(text_lines):
                        next_opt_line = text_lines[m].strip()
                        if re.match(r'^[ABCDE]\)\s+', next_opt_line):
                            break
                        if re.match(r'^\d+\.\s+', next_opt_line):
                            break
                        if next_opt_line:
                            opt_text += " " + next_opt_line
                        m += 1
                    
                    if len(opt_text.strip()) > 2:
                        options[opt_letter] = opt_text.strip()
                    k = m
                else:
                    k += 1
                    if opt_line and re.match(r'^\d+\.\s+', opt_line):
                        break
            
            if options:
                questions.append({
                    'number': q_num,
                    'text': q_text.strip(),
                    'options': options,
                    'test': test_name
                })
            
            i = k
        else:
            i += 1
    
    return questions

def clean_text(text):
    """Metni temizle"""
    if not text:
        return ""
    text = re.sub(r'\s+', ' ', text)
    text = text.strip()
    return text

def questions_to_deneme_csv(all_questions, output_path):
    """Soruları deneme sınavı CSV formatına dönüştür"""
    
    # Soru numaralarına göre sırala
    all_questions.sort(key=lambda x: (x['test'], x['number']))
    
    # Soru numaralarını yeniden numaralandır (1'den başla)
    question_num = 1
    for q in all_questions:
        q['final_number'] = question_num
        question_num += 1
    
    with open(output_path, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        
        # Deneme sınavı CSV header
        writer.writerow([
            'soru_metni', 'sik_a', 'sik_b', 'sik_c', 'sik_d', 'sik_e',
            'dogru_cevap', 'zorluk', 'konular', 'aciklama', 'ders_ad'
        ])
        
        # Soruları yaz
        for q in all_questions:
            question_text = clean_text(q['text'])
            options = q['options']
            test_name = q['test']
            
            # Şıkları soru metninden çıkar
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
            
            if not any([sik_a, sik_b, sik_c, sik_d, sik_e]):
                continue
            
            # Ders adını test adına göre belirle
            ders_ad_map = {
                'Türkçe': 'Türkçe',
                'Matematik': 'Matematik',
                'Fen': 'Fen Bilimleri',
                'Sosyal': 'Sosyal Bilimler'
            }
            ders_ad = ders_ad_map.get(test_name, 'Genel')
            konular = f"TYT {test_name}"
            
            writer.writerow([
                question_text,
                sik_a,
                sik_b,
                sik_c,
                sik_d,
                sik_e,
                "",  # Doğru cevap - manuel doldurulacak
                "",  # Zorluk
                konular,
                "",  # Açıklama
                ders_ad
            ])

def main():
    if len(sys.argv) < 3:
        print("Kullanım: python pdf_to_deneme_sinavi_full.py <input.pdf> <output.csv>")
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
    
    print("Test bölümleri bulunuyor...")
    sections = find_test_sections(text)
    print(f"Bulunan testler: {list(sections.keys())}")
    
    lines = text.split('\n')
    all_questions = []
    
    # Her testten soruları çıkar
    test_order = ['Türkçe', 'Sosyal', 'Matematik', 'Fen']
    for i, test_name in enumerate(test_order):
        if test_name in sections:
            start_idx = sections[test_name]
            end_idx = sections[test_order[i+1]] if i+1 < len(test_order) and test_order[i+1] in sections else None
            print(f"\n{test_name} testinden sorular çıkarılıyor...")
            questions = extract_questions_from_section(lines, start_idx, end_idx, test_name)
            print(f"  {len(questions)} soru bulundu")
            all_questions.extend(questions)
    
    if not all_questions:
        print("Uyarı: Hiç soru bulunamadı.")
        sys.exit(1)
    
    print(f"\nToplam {len(all_questions)} soru bulundu")
    
    if len(all_questions) < 100:
        print(f"⚠️  Uyarı: Beklenen 120 soru yerine {len(all_questions)} soru bulundu.")
    
    print(f"CSV'ye yazılıyor: {output_csv}")
    questions_to_deneme_csv(all_questions, output_csv)
    
    print(f"\n✅ Tamamlandı! {len(all_questions)} soru deneme sınavı CSV formatına yazıldı.")
    print(f"\n⚠️  ÖNEMLİ: CSV dosyasını açıp şunları kontrol edin:")
    print("   1. Doğru cevap sütununu doldurun (A, B, C, D, E)")
    print("   2. Soru metinlerini ve şıkları kontrol edin")
    print(f"\n📝 Sonraki adım:")
    print("   1. Deneme sınavı oluşturun: POST /api/deneme-sinavi")
    print("   2. CSV'yi import edin: POST /api/deneme-sinavlari/{denemeId}/import-csv")

if __name__ == "__main__":
    main()









