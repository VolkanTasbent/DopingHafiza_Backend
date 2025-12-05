#!/usr/bin/env python3
"""
PDF'den TAM 120 Soruyu Çıkarıp Deneme Sınavı CSV'sine Dönüştürücü
40 Türkçe + 20 Sosyal + 40 Matematik + 20 Fen = 120 soru
"""

import sys
import re
import csv
import pdfplumber
from pathlib import Path

def extract_all_text_from_pdf(pdf_path):
    """PDF'den tüm metni çıkar"""
    all_text = ""
    try:
        with pdfplumber.open(pdf_path) as pdf:
            for page in pdf.pages:
                page_text = page.extract_text()
                if page_text:
                    all_text += page_text + "\n"
    except Exception as e:
        print(f"PDF okuma hatası: {e}")
        return None
    return all_text

def find_test_boundaries(text):
    """Test sınırlarını bul"""
    lines = text.split('\n')
    boundaries = {}
    
    for i, line in enumerate(lines):
        if 'TÜRKÇE TESTİ' in line and 'Türkçe' not in boundaries:
            boundaries['Türkçe'] = i
        elif 'SOSYAL BİLİMLER TESTİ' in line and 'Sosyal' not in boundaries:
            boundaries['Sosyal'] = i
        elif ('TEMEL MATEMATİK TESTİ' in line or 'MATEMATİK TESTİ' in line) and 'Matematik' not in boundaries:
            boundaries['Matematik'] = i
        elif 'FEN BİLİMLERİ TESTİ' in line and 'Fen' not in boundaries:
            boundaries['Fen'] = i
    
    return boundaries

def extract_questions_from_text(text, test_name, expected_count):
    """Metinden belirli bir test için tüm soruları çıkar"""
    questions = []
    lines = text.split('\n')
    
    # Test için soru numarası aralığını belirle
    if test_name == 'Türkçe':
        min_q = 1
        max_q = 40
    elif test_name == 'Sosyal':
        min_q = 1
        max_q = 20
    elif test_name == 'Matematik':
        min_q = 1
        max_q = 40
    elif test_name == 'Fen':
        min_q = 1
        max_q = 20
    
    # Tüm satırları birleştirilmiş metin olarak al
    full_text = ' '.join(lines)
    
    # Soru numaralarını bul (örn: "1.", "12.", "40.")
    question_pattern = rf'\b({min_q}|[2-9]|[1-9][0-9]|{max_q})\.\s+'
    
    # Her soru numarası için
    for q_num in range(min_q, max_q + 1):
        q_num_str = str(q_num)
        
        # Soru numarasını bul
        pattern = rf'\b{q_num_str}\.\s+'
        matches = list(re.finditer(pattern, full_text))
        
        if not matches:
            continue
        
        # İlk eşleşmeyi al
        match = matches[0]
        start_pos = match.end()
        
        # Sonraki soru numarasını bul (veya test sonunu)
        next_q_num = q_num + 1
        if next_q_num > max_q:
            # Test sonu - sonraki test başlığını bul
            next_pattern = r'(SOSYAL BİLİMLER TESTİ|TEMEL MATEMATİK TESTİ|MATEMATİK TESTİ|FEN BİLİMLERİ TESTİ|2024.*TESTİ)'
        else:
            next_pattern = rf'\b{next_q_num}\.\s+'
        
        next_match = re.search(next_pattern, full_text[start_pos:])
        if next_match:
            end_pos = start_pos + next_match.start()
        else:
            end_pos = len(full_text)
        
        # Soru metnini çıkar
        question_text = full_text[start_pos:end_pos].strip()
        
        # Şıkları bul (A), B), C), D), E))
        options = {}
        option_patterns = {
            'A': r'A\)\s+([^BCDE\)]+?)(?=[BCDE]\)|$)',
            'B': r'B\)\s+([^CDE\)]+?)(?=[CDE]\)|$)',
            'C': r'C\)\s+([^DE\)]+?)(?=[DE]\)|$)',
            'D': r'D\)\s+([^E\)]+?)(?=E\)|$)',
            'E': r'E\)\s+([^\d]+?)(?=\d+\.|$)'
        }
        
        for opt_letter, opt_pattern in option_patterns.items():
            opt_match = re.search(opt_pattern, question_text, re.DOTALL | re.IGNORECASE)
            if opt_match:
                opt_text = opt_match.group(1).strip()
                # Temizle
                opt_text = re.sub(r'\s+', ' ', opt_text)
                # Çok kısa şıkları atla
                if len(opt_text) > 3:
                    options[opt_letter] = opt_text
        
        # En az 2 şık varsa soruyu ekle
        if len(options) >= 2:
            # Soru metninden şıkları çıkar
            clean_q_text = question_text
            for opt in ['A)', 'B)', 'C)', 'D)', 'E)']:
                clean_q_text = re.sub(rf'{opt}\s+[^\n]+', '', clean_q_text, flags=re.IGNORECASE | re.DOTALL)
            clean_q_text = re.sub(r'\s+', ' ', clean_q_text).strip()
            
            questions.append({
                'number': q_num,
                'text': clean_q_text,
                'options': options,
                'test': test_name
            })
    
    return questions

def extract_questions_improved(text, test_name, expected_count):
    """Geliştirilmiş soru çıkarma - satır bazlı işleme"""
    questions = []
    lines = text.split('\n')
    
    # Test için soru numarası aralığını belirle
    if test_name == 'Türkçe':
        min_q, max_q = 1, 40
    elif test_name == 'Sosyal':
        min_q, max_q = 1, 20
    elif test_name == 'Matematik':
        min_q, max_q = 1, 40
    elif test_name == 'Fen':
        min_q, max_q = 1, 20
    
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        
        # Soru numarası ile başlayan satırı bul
        q_match = re.match(r'^(\d+)\.\s+(.+)$', line)
        if q_match:
            q_num = int(q_match.group(1))
            
            # Soru numarası aralıkta mı?
            if q_num < min_q or q_num > max_q:
                i += 1
                continue
            
            q_text = q_match.group(2)
            
            # Soru metnini topla (sonraki satırlardan)
            j = i + 1
            while j < len(lines):
                next_line = lines[j].strip()
                # Şık başlangıcı varsa dur
                if re.match(r'^[ABCDE]\)\s+', next_line):
                    break
                # Yeni soru numarası varsa dur
                if re.match(r'^\d+\.\s+', next_line):
                    break
                # Boş satır ve sonraki şık değilse dur
                if not next_line:
                    if j + 1 < len(lines) and not re.match(r'^[ABCDE]\)\s+', lines[j+1].strip()):
                        break
                # Soru metnine ekle
                if next_line and not next_line.startswith('Diğer sayfaya'):
                    q_text += " " + next_line
                j += 1
            
            # Şıkları bul
            options = {}
            k = j
            while k < len(lines):
                opt_line = lines[k].strip()
                
                # Şık bulma
                opt_match = re.match(r'^([ABCDE])\)\s+(.+)$', opt_line)
                if opt_match:
                    opt_letter = opt_match.group(1)
                    opt_text = opt_match.group(2)
                    
                    # Şık metnini topla
                    m = k + 1
                    while m < len(lines):
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
                    
                    if len(opt_text.strip()) > 3:
                        options[opt_letter] = opt_text.strip()
                    k = m
                else:
                    k += 1
                    # Yeni soru numarası varsa dur
                    if opt_line and re.match(r'^\d+\.\s+', opt_line):
                        break
            
            # En az 2 şık varsa soruyu ekle
            if len(options) >= 2:
                # Soru metninden şıkları çıkar
                clean_q_text = q_text
                for opt in ['A)', 'B)', 'C)', 'D)', 'E)']:
                    opt_escaped = opt.replace(')', r'\)')
                    clean_q_text = re.sub(rf'{opt_escaped}\s+[^\n]+', '', clean_q_text, flags=re.IGNORECASE)
                clean_q_text = re.sub(r'\s+', ' ', clean_q_text).strip()
                
                questions.append({
                    'number': q_num,
                    'text': clean_q_text,
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

def questions_to_csv(all_questions, output_path):
    """Soruları deneme sınavı CSV formatına dönüştür"""
    
    # Test ve soru numarasına göre sırala
    test_order = ['Türkçe', 'Sosyal', 'Matematik', 'Fen']
    all_questions.sort(key=lambda x: (test_order.index(x['test']), x['number']))
    
    with open(output_path, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        
        # Header
        writer.writerow([
            'soru_metni', 'sik_a', 'sik_b', 'sik_c', 'sik_d', 'sik_e',
            'dogru_cevap', 'zorluk', 'konular', 'aciklama', 'ders_ad'
        ])
        
        # Soruları yaz
        for q in all_questions:
            question_text = clean_text(q['text'])
            options = q['options']
            test_name = q['test']
            
            # Şıkları al
            sik_a = clean_text(options.get('A', ''))
            sik_b = clean_text(options.get('B', ''))
            sik_c = clean_text(options.get('C', ''))
            sik_d = clean_text(options.get('D', ''))
            sik_e = clean_text(options.get('E', ''))
            
            if not any([sik_a, sik_b, sik_c, sik_d, sik_e]):
                continue
            
            # Ders adı
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
                "",  # Doğru cevap
                "",  # Zorluk
                konular,
                "",  # Açıklama
                ders_ad
            ])

def main():
    if len(sys.argv) < 3:
        print("Kullanım: python pdf_parse_120_soru.py <input.pdf> <output.csv>")
        sys.exit(1)
    
    input_pdf = sys.argv[1]
    output_csv = sys.argv[2]
    
    if not Path(input_pdf).exists():
        print(f"Hata: Dosya bulunamadı: {input_pdf}")
        sys.exit(1)
    
    print(f"PDF okunuyor: {input_pdf}")
    text = extract_all_text_from_pdf(input_pdf)
    
    if not text:
        print("Hata: PDF'den metin çıkarılamadı")
        sys.exit(1)
    
    print("Test sınırları bulunuyor...")
    boundaries = find_test_boundaries(text)
    print(f"Test sınırları: {boundaries}")
    
    # Her test için metni ayır
    lines = text.split('\n')
    all_questions = []
    
    test_order = ['Türkçe', 'Sosyal', 'Matematik', 'Fen']
    expected_counts = {'Türkçe': 40, 'Sosyal': 20, 'Matematik': 40, 'Fen': 20}
    
    for test_name in test_order:
        if test_name not in boundaries:
            print(f"⚠️  {test_name} testi bulunamadı!")
            continue
        
        start_idx = boundaries[test_name]
        # Sonraki testin başlangıcını bul
        end_idx = len(lines)
        for next_test in test_order:
            if next_test != test_name and next_test in boundaries:
                if boundaries[next_test] > start_idx:
                    end_idx = boundaries[next_test]
                    break
        
        # Test metnini çıkar
        test_text = '\n'.join(lines[start_idx:end_idx])
        
        print(f"\n{test_name} testi işleniyor (beklenen: {expected_counts[test_name]} soru)...")
        questions = extract_questions_improved(test_text, test_name, expected_counts[test_name])
        
        # Soru numaralarına göre sırala
        questions.sort(key=lambda x: x['number'])
        
        print(f"  ✅ {len(questions)} soru bulundu")
        
        # Eksik soruları kontrol et
        found_nums = {q['number'] for q in questions}
        expected_nums = set(range(1, expected_counts[test_name] + 1))
        missing = expected_nums - found_nums
        if missing:
            print(f"  ⚠️  Eksik soru numaraları: {sorted(list(missing))[:10]}... (toplam {len(missing)} eksik)")
        
        all_questions.extend(questions)
    
    # Test bazında özet
    print(f"\n{'='*60}")
    print(f"TOPLAM {len(all_questions)} SORU BULUNDU")
    print(f"{'='*60}")
    
    test_counts = {}
    for q in all_questions:
        test_name = q['test']
        test_counts[test_name] = test_counts.get(test_name, 0) + 1
    
    for test_name in test_order:
        count = test_counts.get(test_name, 0)
        expected = expected_counts[test_name]
        status = "✅" if count == expected else "⚠️"
        print(f"{status} {test_name}: {count}/{expected} soru")
    
    if len(all_questions) < 120:
        print(f"\n⚠️  UYARI: Toplam {len(all_questions)} soru bulundu, 120 soru bekleniyordu!")
        print("   PDF formatı karmaşık olabilir, bazı sorular parse edilememiş olabilir.")
    else:
        print(f"\n✅ TAM 120 SORU BULUNDU!")
    
    print(f"\nCSV'ye yazılıyor: {output_csv}")
    questions_to_csv(all_questions, output_csv)
    
    print(f"\n✅ Tamamlandı! {len(all_questions)} soru CSV'ye yazıldı.")
    print(f"\n⚠️  ÖNEMLİ: CSV dosyasını açıp şunları kontrol edin:")
    print("   1. Doğru cevap sütununu doldurun (A, B, C, D, E)")
    print("   2. Soru metinlerini ve şıkları kontrol edin")
    print(f"\n📝 Sonraki adım:")
    print("   1. Deneme sınavı oluşturun: POST /api/deneme-sinavi")
    print("   2. CSV'yi import edin: POST /api/deneme-sinavlari/{{denemeId}}/import-csv")

if __name__ == "__main__":
    main()

