#!/usr/bin/env python3
"""
ULTIMATE PDF Parser - Her sayfayı satır satır işleyerek TAM 120 soru çıkarır
"""

import sys
import re
import csv
import pdfplumber
from pathlib import Path

def parse_page_by_lines(page_text, test_name, min_q, max_q):
    """Sayfa metnini satır satır işleyerek soruları çıkar"""
    questions = []
    lines = page_text.split('\n')
    
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        
        # Soru numarası ile başlayan satır
        q_match = re.match(r'^(\d+)\.\s+(.+)$', line)
        if q_match:
            q_num = int(q_match.group(1))
            
            if q_num < min_q or q_num > max_q:
                i += 1
                continue
            
            q_text = q_match.group(2)
            
            # Soru metnini topla
            j = i + 1
            while j < len(lines):
                next_line = lines[j].strip()
                if re.match(r'^[ABCDE]\)\s+', next_line):
                    break
                if re.match(r'^\d+\.\s+', next_line):
                    break
                if next_line:
                    q_text += " " + next_line
                j += 1
            
            # Şıkları topla
            options = {}
            k = j
            while k < len(lines):
                opt_line = lines[k].strip()
                opt_match = re.match(r'^([ABCDE])\)\s+(.+)$', opt_line)
                if opt_match:
                    opt_letter = opt_match.group(1)
                    opt_text = opt_match.group(2)
                    
                    # Şık metnini topla
                    m = k + 1
                    while m < len(lines):
                        next_opt = lines[m].strip()
                        if re.match(r'^[ABCDE]\)\s+', next_opt):
                            break
                        if re.match(r'^\d+\.\s+', next_opt):
                            break
                        if next_opt:
                            opt_text += " " + next_opt
                        m += 1
                    
                    opt_text = re.sub(r'\s+', ' ', opt_text).strip()
                    if len(opt_text) > 2:
                        options[opt_letter] = opt_text
                    k = m
                else:
                    k += 1
                    if opt_line and re.match(r'^\d+\.\s+', opt_line):
                        break
            
            if len(options) >= 1:  # En az 1 şık yeterli
                q_text = re.sub(r'\s+', ' ', q_text).strip()
                # Şıkları soru metninden çıkar
                for opt in ['A)', 'B)', 'C)', 'D)', 'E)']:
                    opt_escaped = opt.replace(')', r'\)')
                    q_text = re.sub(rf'{opt_escaped}\s+[^\n]+', '', q_text, flags=re.IGNORECASE)
                q_text = re.sub(r'\s+', ' ', q_text).strip()
                
                if len(q_text) > 5:
                    questions.append({
                        'number': q_num,
                        'text': q_text,
                        'options': options,
                        'test': test_name
                    })
            
            i = k
        else:
            i += 1
    
    return questions

def extract_all_questions(pdf_path):
    """PDF'den tüm soruları çıkar"""
    
    with pdfplumber.open(pdf_path) as pdf:
        # Test sayfalarını bul
        test_pages = {}
        for i, page in enumerate(pdf.pages):
            text = page.extract_text() or ''
            if 'TÜRKÇE TESTİ' in text and 'Türkçe' not in test_pages:
                test_pages['Türkçe'] = i
            elif 'SOSYAL BİLİMLER TESTİ' in text and 'Sosyal' not in test_pages:
                test_pages['Sosyal'] = i
            elif ('TEMEL MATEMATİK TESTİ' in text or 'MATEMATİK TESTİ' in text) and 'Matematik' not in test_pages:
                test_pages['Matematik'] = i
            elif 'FEN BİLİMLERİ TESTİ' in text and 'Fen' not in test_pages:
                test_pages['Fen'] = i
        
        print(f"Test sayfaları: {[(k, v+1) for k, v in test_pages.items()]}\n")
        
        all_questions = []
        test_order = ['Türkçe', 'Sosyal', 'Matematik', 'Fen']
        expected_counts = {'Türkçe': 40, 'Sosyal': 20, 'Matematik': 40, 'Fen': 20}
        
        for test_name in test_order:
            if test_name not in test_pages:
                continue
            
            start_page = test_pages[test_name]
            end_page = len(pdf.pages)
            for next_test in test_order:
                if next_test != test_name and next_test in test_pages:
                    if test_pages[next_test] > start_page:
                        end_page = test_pages[next_test]
                        break
            
            if test_name == 'Türkçe':
                min_q, max_q = 1, 40
            elif test_name == 'Sosyal':
                min_q, max_q = 1, 20
            elif test_name == 'Matematik':
                min_q, max_q = 1, 40
            elif test_name == 'Fen':
                min_q, max_q = 1, 20
            
            print(f"{test_name} testi işleniyor (sayfa {start_page+1}-{end_page})...")
            
            # Her sayfayı ayrı işle
            test_questions = {}
            for page_idx in range(start_page, end_page):
                page = pdf.pages[page_idx]
                text = page.extract_text() or ''
                page_questions = parse_page_by_lines(text, test_name, min_q, max_q)
                
                for q in page_questions:
                    # Aynı soru numarası varsa birleştir
                    if q['number'] in test_questions:
                        # Şıkları birleştir
                        existing = test_questions[q['number']]
                        for opt, text in q['options'].items():
                            if opt not in existing['options']:
                                existing['options'][opt] = text
                    else:
                        test_questions[q['number']] = q
            
            questions = sorted(test_questions.values(), key=lambda x: x['number'])
            print(f"  ✅ {len(questions)} soru bulundu (beklenen: {expected_counts[test_name]})")
            
            all_questions.extend(questions)
    
    return all_questions

def clean_text(text):
    if not text:
        return ""
    return re.sub(r'\s+', ' ', text).strip()

def questions_to_csv(all_questions, output_path):
    """Soruları CSV formatına dönüştür"""
    
    test_order = ['Türkçe', 'Sosyal', 'Matematik', 'Fen']
    all_questions.sort(key=lambda x: (test_order.index(x['test']), x['number']))
    
    with open(output_path, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        
        writer.writerow([
            'soru_metni', 'sik_a', 'sik_b', 'sik_c', 'sik_d', 'sik_e',
            'dogru_cevap', 'zorluk', 'konular', 'aciklama', 'ders_ad'
        ])
        
        for q in all_questions:
            question_text = clean_text(q['text'])
            options = q['options']
            test_name = q['test']
            
            sik_a = clean_text(options.get('A', ''))
            sik_b = clean_text(options.get('B', ''))
            sik_c = clean_text(options.get('C', ''))
            sik_d = clean_text(options.get('D', ''))
            sik_e = clean_text(options.get('E', ''))
            
            if not any([sik_a, sik_b, sik_c, sik_d, sik_e]):
                continue
            
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
                "",
                "",
                konular,
                "",
                ders_ad
            ])

def main():
    if len(sys.argv) < 3:
        print("Kullanım: python pdf_parse_ultimate.py <input.pdf> <output.csv>")
        sys.exit(1)
    
    input_pdf = sys.argv[1]
    output_csv = sys.argv[2]
    
    if not Path(input_pdf).exists():
        print(f"Hata: Dosya bulunamadı: {input_pdf}")
        sys.exit(1)
    
    print(f"PDF okunuyor: {input_pdf}\n")
    
    all_questions = extract_all_questions(input_pdf)
    
    test_counts = {}
    for q in all_questions:
        test_counts[q['test']] = test_counts.get(q['test'], 0) + 1
    
    print(f"\n{'='*60}")
    print(f"TOPLAM {len(all_questions)} SORU BULUNDU")
    print(f"{'='*60}")
    
    expected_counts = {'Türkçe': 40, 'Sosyal': 20, 'Matematik': 40, 'Fen': 20}
    for test_name in ['Türkçe', 'Sosyal', 'Matematik', 'Fen']:
        count = test_counts.get(test_name, 0)
        expected = expected_counts[test_name]
        status = "✅" if count == expected else "⚠️"
        print(f"{status} {test_name}: {count}/{expected} soru")
    
    if len(all_questions) == 120:
        print(f"\n🎉 TAM 120 SORU BULUNDU!")
    else:
        print(f"\n⚠️  {len(all_questions)}/120 soru bulundu")
    
    print(f"\nCSV'ye yazılıyor: {output_csv}")
    questions_to_csv(all_questions, output_csv)
    
    print(f"\n✅ Tamamlandı! {len(all_questions)} soru CSV'ye yazıldı.")

if __name__ == "__main__":
    main()

