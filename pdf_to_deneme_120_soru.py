#!/usr/bin/env python3
"""
PDF'den Tüm 120 Soruyu Çıkarıp Deneme Sınavı CSV'sine Dönüştürücü
TYT kitapçığı için - 40 Türkçe + 40 Matematik + 20 Fen + 20 Sosyal = 120 soru
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

def find_questions_by_test_pages(pdf_path):
    """
    PDF sayfalarını kullanarak test başlıklarını bul ve soruları çıkar
    """
    with pdfplumber.open(pdf_path) as pdf:
        # Test başlıklarının bulunduğu sayfaları bul
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
        
        print(f"Test sayfaları bulundu: {[(k, v+1) for k, v in test_pages.items()]}")
        
        # Her test için soruları çıkar
        all_questions = []
        test_order = ['Türkçe', 'Sosyal', 'Matematik', 'Fen']
        expected_counts = {'Türkçe': 40, 'Sosyal': 20, 'Matematik': 40, 'Fen': 20}
        
        for test_name in test_order:
            if test_name not in test_pages:
                continue
            
            start_page = test_pages[test_name]
            # Sonraki testin başlangıç sayfasını bul
            end_page = len(pdf.pages)
            for next_test in test_order:
                if next_test != test_name and next_test in test_pages:
                    if test_pages[next_test] > start_page:
                        end_page = test_pages[next_test]
                        break
            
            print(f"\n{test_name} testi işleniyor (sayfa {start_page+1}-{end_page})...")
            questions = extract_questions_from_pages(pdf, start_page, end_page, test_name, expected_counts[test_name])
            print(f"  {len(questions)} soru bulundu (beklenen: {expected_counts[test_name]})")
            all_questions.extend(questions)
        
        return all_questions

def extract_questions_from_pages(pdf, start_page, end_page, test_name, expected_count):
    """Belirli sayfa aralığından soruları çıkar"""
    # Tüm sayfaların metnini birleştir
    all_text = ""
    for page_idx in range(start_page, end_page):
        page = pdf.pages[page_idx]
        text = page.extract_text() or ''
        all_text += text + "\n"
    
    lines = all_text.split('\n')
    return extract_questions_from_range(lines, 0, len(lines), test_name, expected_count)

def find_questions_by_test(text):
    """
    Test başlıklarına göre soruları bul ve ayır
    """
    lines = text.split('\n')
    
    # Test başlıklarını bul
    test_starts = {}
    current_test = None
    
    for i, line in enumerate(lines):
        if 'TÜRKÇE TESTİ' in line and 'Türkçe' not in test_starts:
            test_starts['Türkçe'] = i
            current_test = 'Türkçe'
        elif 'SOSYAL BİLİMLER TESTİ' in line or 'SOSYAL' in line and 'Sosyal' not in test_starts:
            test_starts['Sosyal'] = i
            current_test = 'Sosyal'
        elif ('TEMEL MATEMATİK TESTİ' in line or 'MATEMATİK TESTİ' in line) and 'Matematik' not in test_starts:
            test_starts['Matematik'] = i
            current_test = 'Matematik'
        elif 'FEN BİLİMLERİ TESTİ' in line and 'Fen' not in test_starts:
            test_starts['Fen'] = i
            current_test = 'Fen'
    
    print(f"Test başlıkları bulundu: {list(test_starts.keys())}")
    
    # Her test için soruları çıkar
    all_questions = []
    test_order = ['Türkçe', 'Sosyal', 'Matematik', 'Fen']
    expected_counts = {'Türkçe': 40, 'Sosyal': 20, 'Matematik': 40, 'Fen': 20}
    
    for test_name in test_order:
        if test_name not in test_starts:
            continue
        
        start_idx = test_starts[test_name]
        # Sonraki testin başlangıcını bul
        end_idx = len(lines)
        for next_test in test_order:
            if next_test != test_name and next_test in test_starts:
                if test_starts[next_test] > start_idx:
                    end_idx = test_starts[next_test]
                    break
        
        print(f"\n{test_name} testi işleniyor (satır {start_idx}-{end_idx})...")
        questions = extract_questions_from_range(lines, start_idx, end_idx, test_name, expected_counts[test_name])
        print(f"  {len(questions)} soru bulundu (beklenen: {expected_counts[test_name]})")
        all_questions.extend(questions)
    
    return all_questions

def extract_questions_from_range(lines, start_idx, end_idx, test_name, expected_count):
    """Belirli bir satır aralığından soruları çıkar"""
    questions = []
    i = start_idx
    
    while i < end_idx:
        line = lines[i].strip()
        
        # Soru numarası ile başlayan satırı bul (örn: "1.", "12.", "40.")
        q_match = re.match(r'^(\d+)\.\s+(.+)$', line)
        if q_match:
            q_num = int(q_match.group(1))
            q_text = q_match.group(2)
            
            # Soru numarası beklenen aralıkta mı kontrol et
            if test_name == 'Türkçe' and q_num > 40:
                i += 1
                continue
            elif test_name == 'Sosyal' and q_num > 20:
                i += 1
                continue
            elif test_name == 'Matematik' and q_num > 40:
                i += 1
                continue
            elif test_name == 'Fen' and q_num > 20:
                i += 1
                continue
            
            # Soru metnini topla (sonraki satırlardan devam edebilir)
            j = i + 1
            while j < end_idx:
                next_line = lines[j].strip()
                # Şık başlangıcı varsa dur
                if re.match(r'^[ABCDE]\)\s+', next_line):
                    break
                # Yeni soru numarası varsa dur
                if re.match(r'^\d+\.\s+', next_line):
                    break
                # Soru metnine ekle
                if next_line and not next_line.startswith('Diğer sayfaya'):
                    q_text += " " + next_line
                j += 1
            
            # Şıkları bul
            options = {}
            k = j
            option_count = 0
            while k < end_idx and option_count < 5:
                opt_line = lines[k].strip()
                
                # Şık bulma
                opt_match = re.match(r'^([ABCDE])\)\s+(.+)$', opt_line)
                if opt_match:
                    opt_letter = opt_match.group(1)
                    opt_text = opt_match.group(2)
                    
                    # Şık metnini topla
                    m = k + 1
                    while m < end_idx:
                        next_opt_line = lines[m].strip()
                        # Yeni şık başlangıcı varsa dur
                        if re.match(r'^[ABCDE]\)\s+', next_opt_line):
                            break
                        # Yeni soru numarası varsa dur
                        if re.match(r'^\d+\.\s+', next_opt_line):
                            break
                        # Şık metnine ekle
                        if next_opt_line and not next_opt_line.startswith('Diğer sayfaya'):
                            opt_text += " " + next_opt_line
                        m += 1
                    
                    if len(opt_text.strip()) > 2:
                        options[opt_letter] = opt_text.strip()
                        option_count += 1
                    k = m
                else:
                    k += 1
                    # Şık toplama bitti mi kontrol et
                    if opt_line and re.match(r'^\d+\.\s+', opt_line):
                        break
            
            # En az 2 şık varsa soruyu ekle
            if len(options) >= 2:
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
    
    # Test ve soru numarasına göre sırala
    all_questions.sort(key=lambda x: (['Türkçe', 'Sosyal', 'Matematik', 'Fen'].index(x['test']), x['number']))
    
    # Global soru numarası (1'den 120'ye)
    global_num = 1
    for q in all_questions:
        q['global_number'] = global_num
        global_num += 1
    
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
        print("Kullanım: python pdf_to_deneme_120_soru.py <input.pdf> <output.csv>")
        sys.exit(1)
    
    input_pdf = sys.argv[1]
    output_csv = sys.argv[2]
    
    if not Path(input_pdf).exists():
        print(f"Hata: Dosya bulunamadı: {input_pdf}")
        sys.exit(1)
    
    print(f"PDF okunuyor: {input_pdf}")
    
    print("Sorular parse ediliyor...")
    all_questions = find_questions_by_test_pages(input_pdf)
    
    if not all_questions:
        print("Uyarı: Hiç soru bulunamadı.")
        sys.exit(1)
    
    # Test bazında say
    test_counts = {}
    for q in all_questions:
        test_name = q['test']
        test_counts[test_name] = test_counts.get(test_name, 0) + 1
    
    print(f"\n{'='*50}")
    print(f"Toplam {len(all_questions)} soru bulundu")
    print(f"{'='*50}")
    for test_name in ['Türkçe', 'Sosyal', 'Matematik', 'Fen']:
        count = test_counts.get(test_name, 0)
        expected = {'Türkçe': 40, 'Sosyal': 20, 'Matematik': 40, 'Fen': 20}[test_name]
        status = "✅" if count == expected else "⚠️"
        print(f"{status} {test_name}: {count} soru (beklenen: {expected})")
    
    print(f"\nCSV'ye yazılıyor: {output_csv}")
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

