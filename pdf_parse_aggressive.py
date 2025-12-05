#!/usr/bin/env python3
"""
AGRESIF PDF Parser - TAM 120 Soru Çıkarma
Her sayfayı ayrı işleyip, soru numaralarını ve şıkları daha esnek bulur
"""

import sys
import re
import csv
import pdfplumber
from pathlib import Path
from collections import defaultdict

def extract_questions_aggressive(pdf_path):
    """PDF'den agresif şekilde tüm soruları çıkar"""
    
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
        
        print(f"Test sayfaları: {[(k, v+1) for k, v in test_pages.items()]}")
        
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
            
            # Test için soru numarası aralığı
            if test_name == 'Türkçe':
                min_q, max_q = 1, 40
            elif test_name == 'Sosyal':
                min_q, max_q = 1, 20
            elif test_name == 'Matematik':
                min_q, max_q = 1, 40
            elif test_name == 'Fen':
                min_q, max_q = 1, 20
            
            # Tüm sayfaların metnini birleştir
            full_text = ""
            for page_idx in range(start_page, end_page):
                page = pdf.pages[page_idx]
                text = page.extract_text() or ''
                full_text += text + "\n"
            
            # Her soru numarası için ara
            questions = []
            found_numbers = set()
            
            for q_num in range(min_q, max_q + 1):
                q_num_str = str(q_num)
                
                # Soru numarasını bul - TÜM eşleşmeleri kontrol et (çok esnek)
                patterns = [
                    rf'\b{q_num_str}\.\s+',  # "1. "
                    rf'^{q_num_str}\.\s+',   # Satır başında
                    rf'\s{q_num_str}\.\s+',  # Boşluk + "1. "
                    rf'{q_num_str}\.\s+',    # Herhangi bir yerde "1. "
                    rf'\b{q_num_str}\s+',    # "1 " (nokta olmadan)
                    rf'{q_num_str}\s+\.',    # "1 ." (nokta sonra)
                    rf'{q_num_str}\s-\s+',   # "39 - 40. soruları" formatı
                ]
                
                found = False
                all_matches = []
                for pattern in patterns:
                    matches = list(re.finditer(pattern, full_text, re.MULTILINE))
                    all_matches.extend(matches)
                
                if all_matches:
                    # Tüm eşleşmeleri dene, en iyisini seç
                    best_match = None
                    best_score = 0
                    
                    for match in all_matches:
                        start_pos = match.end()
                        
                        # Sonraki soru numarasını veya test sonunu bul
                        next_q = q_num + 1
                        if next_q > max_q:
                            # Test sonu - daha geniş aralık al
                            search_end = min(start_pos + 5000, len(full_text))
                            # Test başlığını ara
                            end_pattern = r'(SOSYAL BİLİMLER TESTİ|TEMEL MATEMATİK TESTİ|MATEMATİK TESTİ|FEN BİLİMLERİ TESTİ|2024.*TESTİ)'
                            next_match = re.search(end_pattern, full_text[start_pos:search_end], re.MULTILINE)
                            if next_match:
                                end_pos = start_pos + next_match.start()
                            else:
                                # Test sonu - tüm kalan metni al
                                end_pos = search_end
                        else:
                            end_pattern = rf'\b{next_q}\.\s+'
                            next_match = re.search(end_pattern, full_text[start_pos:], re.MULTILINE)
                            if next_match:
                                end_pos = start_pos + next_match.start()
                            else:
                                # Sonraki soru bulunamadı - daha geniş aralık dene
                                search_end = min(start_pos + 5000, len(full_text))
                                # Sonraki soru numarasını daha geniş aralıkta ara
                                wider_pattern = rf'\b({q_num+1}|{q_num+2}|{q_num+3}|{q_num+4}|{q_num+5})\.\s+'
                                wider_match = re.search(wider_pattern, full_text[start_pos:search_end], re.MULTILINE)
                                if wider_match:
                                    end_pos = start_pos + wider_match.start()
                                else:
                                    # Test sonu olabilir - tüm kalan metni al
                                    end_pos = search_end
                        
                        # Soru bloğunu çıkar
                        question_block = full_text[start_pos:end_pos]
                        
                        # Şıkları bul - ÇOK AGRESIF yaklaşım
                        options = {}
                        
                        # Önce tüm şıkları bul (soru bloğunda)
                        for opt_letter in ['A', 'B', 'C', 'D', 'E']:
                            # Çok esnek pattern'ler
                            opt_patterns = [
                                rf'{opt_letter}\)\s+([^ABCDE\)]+?)(?=[BCDE]\)|$)',
                                rf'{opt_letter}\)\s+([^\n]+)',
                                rf'{opt_letter}\)\s+(.+?)(?=\d+\.|$)',
                                rf'{opt_letter}\)\s+(.+?)(?=\s+[BCDE]\)|$)',
                                rf'{opt_letter}\)\s+(.+?)(?=\n|$)',
                                rf'{opt_letter}\)\s+(.+?)(?=\s+\d+\.|$)',
                            ]
                            
                            for opt_pattern in opt_patterns:
                                opt_match = re.search(opt_pattern, question_block, re.DOTALL | re.IGNORECASE)
                                if opt_match:
                                    opt_text = opt_match.group(1).strip()
                                    opt_text = re.sub(r'\s+', ' ', opt_text)
                                    # Çok kısa şıkları atla
                                    if len(opt_text) > 1:  # En az 2 karakter
                                        options[opt_letter] = opt_text
                                        break
                        
                        # Eğer şık bulunamadıysa, daha geniş aralıkta ara
                        if len(options) < 2:  # En az 2 şık bulmaya çalış
                            # Soru bloğundan sonraki 1000 karakteri de kontrol et
                            extended_block = full_text[start_pos:min(start_pos + 4000, len(full_text))]
                            for opt_letter in ['A', 'B', 'C', 'D', 'E']:
                                if opt_letter in options:
                                    continue  # Zaten bulundu
                                # Daha esnek pattern
                                opt_patterns = [
                                    rf'{opt_letter}\)\s+([^\n]+)',
                                    rf'{opt_letter}\)\s+([^ABCDE\)]+)',
                                    rf'\s{opt_letter}\)\s+([^\n]+)',
                                ]
                                for opt_pattern in opt_patterns:
                                    opt_match = re.search(opt_pattern, extended_block, re.IGNORECASE)
                                    if opt_match:
                                        opt_text = opt_match.group(1).strip()
                                        opt_text = re.sub(r'\s+', ' ', opt_text)
                                        if len(opt_text) > 1:
                                            options[opt_letter] = opt_text
                                            break
                        
                        # Şık yoksa bile soruyu ekle (sadece soru metni ile)
                        # Ama önce soru metnini temizle
                        q_text = question_block
                        for opt in ['A)', 'B)', 'C)', 'D)', 'E)']:
                            opt_escaped = opt.replace(')', r'\)')
                            q_text = re.sub(rf'{opt_escaped}\s+[^\n]+', '', q_text, flags=re.IGNORECASE | re.DOTALL)
                        q_text = re.sub(r'\s+', ' ', q_text).strip()
                        
                        # Soru bloğu kalitesini değerlendir (şık sayısı + metin uzunluğu)
                        score = len(options) * 10 + len(q_text)
                        if score > best_score:
                            best_score = score
                            best_match = {
                                'start_pos': start_pos,
                                'end_pos': end_pos,
                                'question_block': question_block,
                                'q_text': q_text,
                                'options': options.copy()
                            }
                    
                    # En iyi eşleşmeyi kullan
                    if best_match and best_score > 0:
                        q_text = best_match['q_text']
                        options = best_match['options']
                        
                        # Soru metni varsa MUTLAKA ekle
                        if len(q_text) > 3:
                            questions.append({
                                'number': q_num,
                                'text': q_text,
                                'options': options,
                                'test': test_name
                            })
                            found_numbers.add(q_num)
                            found = True
                
                if not found:
                    # Soru bulunamadı - daha agresif ara
                    # Belki soru numarası farklı formatta
                    alt_patterns = [
                        rf'{q_num_str}\.',
                        rf'\s{q_num_str}\s',
                        rf'^{q_num_str}\s',
                    ]
                    for alt_pattern in alt_patterns:
                        alt_match = re.search(alt_pattern, full_text, re.MULTILINE)
                        if alt_match:
                            start_pos = alt_match.end()
                            # Sonraki soru numarasını bul
                            next_q = q_num + 1
                            if next_q > max_q:
                                end_pos = min(start_pos + 3000, len(full_text))
                            else:
                                next_pattern = rf'\b{next_q}\.\s+'
                                next_match = re.search(next_pattern, full_text[start_pos:], re.MULTILINE)
                                if next_match:
                                    end_pos = start_pos + next_match.start()
                                else:
                                    end_pos = min(start_pos + 3000, len(full_text))
                            
                            question_block = full_text[start_pos:end_pos]
                            q_text = re.sub(r'\s+', ' ', question_block).strip()
                            
                            # Şıkları bul
                            options = {}
                            for opt_letter in ['A', 'B', 'C', 'D', 'E']:
                                opt_match = re.search(rf'{opt_letter}\)\s+([^\n]+)', question_block, re.IGNORECASE)
                                if opt_match:
                                    opt_text = opt_match.group(1).strip()
                                    if len(opt_text) > 1:
                                        options[opt_letter] = opt_text
                            
                            if len(q_text) > 5:
                                questions.append({
                                    'number': q_num,
                                    'text': q_text,
                                    'options': options,
                                    'test': test_name
                                })
                                found = True
                                break
            
            # Özel formatları işle: "39 - 40. soruları" gibi
            if test_name == 'Türkçe':
                # "39 - 40. soruları" formatını bul
                combined_pattern = r'39\s+-\s+40\.\s+soruları'
                combined_match = re.search(combined_pattern, full_text, re.IGNORECASE)
                if combined_match:
                    # 39 ve 40. soruları ayrı ayrı bul
                    start_pos = combined_match.end()
                    # Sonraki test başlığını bul
                    end_pos = len(full_text)
                    next_test_match = re.search(r'(SOSYAL BİLİMLER TESTİ|TEMEL MATEMATİK TESTİ)', full_text[start_pos:], re.IGNORECASE)
                    if next_test_match:
                        end_pos = start_pos + next_test_match.start()
                    
                    combined_block = full_text[start_pos:end_pos]
                    
                    # 39. soruyu bul
                    q39_match = re.search(r'39\.\s+', combined_block, re.IGNORECASE)
                    if q39_match:
                        q39_start = q39_match.end()
                        q40_match = re.search(r'40\.\s+', combined_block[q39_start:], re.IGNORECASE)
                        if q40_match:
                            q39_block = combined_block[q39_start:q39_start+q40_match.start()]
                            q40_block = combined_block[q39_start+q40_match.end():]
                            
                            # 39. soruyu parse et
                            q39_text = re.sub(r'\s+', ' ', q39_block).strip()
                            q39_opts = {}
                            for opt in ['A', 'B', 'C', 'D', 'E']:
                                opt_match = re.search(rf'{opt}\)\s+([^\n]+)', q39_block, re.IGNORECASE)
                                if opt_match:
                                    q39_opts[opt] = opt_match.group(1).strip()
                            
                            if len(q39_text) > 5:
                                questions.append({
                                    'number': 39,
                                    'text': q39_text,
                                    'options': q39_opts,
                                    'test': test_name
                                })
                            
                            # 40. soruyu parse et
                            q40_text = re.sub(r'\s+', ' ', q40_block).strip()
                            q40_opts = {}
                            for opt in ['A', 'B', 'C', 'D', 'E']:
                                opt_match = re.search(rf'{opt}\)\s+([^\n]+)', q40_block, re.IGNORECASE)
                                if opt_match:
                                    q40_opts[opt] = opt_match.group(1).strip()
                            
                            if len(q40_text) > 5:
                                questions.append({
                                    'number': 40,
                                    'text': q40_text,
                                    'options': q40_opts,
                                    'test': test_name
                                })
            
            # Soru numaralarına göre sırala
            questions.sort(key=lambda x: x['number'])
            
            # Eksik soruları boş olarak ekle (kullanıcı elle dolduracak)
            found_nums = {q['number'] for q in questions}
            missing_nums = sorted(set(range(min_q, max_q + 1)) - found_nums)
            
            if missing_nums:
                print(f"  ⚠️  Eksik soru numaraları: {missing_nums} (boş olarak eklenecek)")
                for missing_q in missing_nums:
                    questions.append({
                        'number': missing_q,
                        'text': '',  # Boş - kullanıcı elle dolduracak
                        'options': {},  # Boş
                        'test': test_name
                    })
            
            # Eksik sorular zaten yukarıda boş olarak eklendi, burada tekrar arama yapmaya gerek yok
            if False:  # Bu bloğu devre dışı bırak
                print(f"  ⚠️  Eksik soru numaraları: {missing_nums}")
                # Eksik soruları manuel bul - ÇOK AGRESIF
                for missing_q in missing_nums:
                    missing_str = str(missing_q)
                    # Çok agresif pattern'ler
                    patterns = [
                        rf'\b{missing_str}\.\s+',
                        rf'{missing_str}\.\s+',
                        rf'{missing_str}\.',
                        rf'\s{missing_str}\s+',
                        rf'^{missing_str}\s+',
                        rf'{missing_str}\s',
                        rf'{missing_str}\s-\s+',  # "39 - 40. soruları" formatı
                        rf'{missing_str}\s+\.',   # "39 ." formatı
                    ]
                    
                    found_missing = False
                    # Önce tüm '39' veya '37' veya '38' geçen yerleri bul
                    all_number_matches = list(re.finditer(rf'\b{missing_str}\b', full_text, re.MULTILINE))
                    if all_number_matches:
                        print(f"    {missing_q}. soru için {len(all_number_matches)} potansiyel eşleşme bulundu")
                    
                    for pattern in patterns:
                        matches = list(re.finditer(pattern, full_text, re.MULTILINE))
                        if matches:
                            # Tüm eşleşmeleri dene
                            for match in matches:
                                start_pos = match.end()
                                
                                # Sonraki soru veya test sonu
                                if missing_q == max_q:
                                    # Test sonu - daha geniş aralık
                                    end_pos = min(start_pos + 5000, len(full_text))
                                else:
                                    # Sonraki soru numarasını bul
                                    next_patterns = [
                                        rf'\b{missing_q+1}\.\s+',
                                        rf'{missing_q+1}\.',
                                        rf'\b{missing_q+2}\.\s+',
                                    ]
                                    end_pos = min(start_pos + 5000, len(full_text))
                                    for next_pattern in next_patterns:
                                        next_match = re.search(next_pattern, full_text[start_pos:], re.MULTILINE)
                                        if next_match:
                                            end_pos = start_pos + next_match.start()
                                            break
                                
                                block = full_text[start_pos:end_pos]
                                q_text = re.sub(r'\s+', ' ', block).strip()
                                
                                # Şıkları bul - çok agresif
                                opts = {}
                                for opt in ['A', 'B', 'C', 'D', 'E']:
                                    opt_patterns = [
                                        rf'{opt}\)\s+([^\n]+)',
                                        rf'{opt}\)\s+([^ABCDE\)]+)',
                                        rf'\s{opt}\)\s+([^\n]+)',
                                    ]
                                    for opt_pattern in opt_patterns:
                                        opt_match = re.search(opt_pattern, block, re.IGNORECASE | re.DOTALL)
                                        if opt_match:
                                            opt_text = opt_match.group(1).strip()
                                            opt_text = re.sub(r'\s+', ' ', opt_text)
                                            if len(opt_text) > 1:
                                                opts[opt] = opt_text
                                                break
                                
                                # Soru metnini temizle
                                for opt in ['A)', 'B)', 'C)', 'D)', 'E)']:
                                    opt_escaped = opt.replace(')', r'\)')
                                    q_text = re.sub(rf'{opt_escaped}\s+[^\n]+', '', q_text, flags=re.IGNORECASE)
                                q_text = re.sub(r'\s+', ' ', q_text).strip()
                                
                                # Soru metni varsa MUTLAKA ekle (uzunluk kontrolü yok)
                                if q_text:  # Boş değilse
                                    questions.append({
                                        'number': missing_q,
                                        'text': q_text if len(q_text) > 3 else block[:500],  # Çok kısa ise tüm bloğu al
                                        'options': opts,
                                        'test': test_name
                                    })
                                    found_missing = True
                                    break
                            
                            if found_missing:
                                break
            
            # Tekrar sırala
            questions.sort(key=lambda x: x['number'])
            
            print(f"  ✅ {len(questions)} soru bulundu (beklenen: {expected_counts[test_name]})")
            
            all_questions.extend(questions)
    
    return all_questions

def clean_text(text):
    """Metni temizle"""
    if not text:
        return ""
    text = re.sub(r'\s+', ' ', text)
    text = text.strip()
    return text

def questions_to_csv(all_questions, output_path):
    """Soruları CSV formatına dönüştür"""
    
    # Test ve soru numarasına göre sırala
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
            
            # Boş soruları da ekle (kullanıcı elle dolduracak)
            # if not any([sik_a, sik_b, sik_c, sik_d, sik_e]):
            #     continue
            
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
        print("Kullanım: python pdf_parse_aggressive.py <input.pdf> <output.csv>")
        sys.exit(1)
    
    input_pdf = sys.argv[1]
    output_csv = sys.argv[2]
    
    if not Path(input_pdf).exists():
        print(f"Hata: Dosya bulunamadı: {input_pdf}")
        sys.exit(1)
    
    print(f"PDF okunuyor: {input_pdf}")
    print("Agresif parsing başlatılıyor...\n")
    
    all_questions = extract_questions_aggressive(input_pdf)
    
    # Test bazında özet
    test_counts = defaultdict(int)
    for q in all_questions:
        test_counts[q['test']] += 1
    
    print(f"\n{'='*60}")
    print(f"TOPLAM {len(all_questions)} SORU BULUNDU")
    print(f"{'='*60}")
    
    expected_counts = {'Türkçe': 40, 'Sosyal': 20, 'Matematik': 40, 'Fen': 20}
    for test_name in ['Türkçe', 'Sosyal', 'Matematik', 'Fen']:
        count = test_counts.get(test_name, 0)
        expected = expected_counts[test_name]
        status = "✅" if count == expected else "⚠️"
        print(f"{status} {test_name}: {count}/{expected} soru")
    
    if len(all_questions) < 120:
        print(f"\n⚠️  UYARI: {len(all_questions)}/120 soru bulundu!")
    else:
        print(f"\n✅ TAM 120 SORU BULUNDU!")
    
    print(f"\nCSV'ye yazılıyor: {output_csv}")
    questions_to_csv(all_questions, output_csv)
    
    print(f"\n✅ Tamamlandı! {len(all_questions)} soru CSV'ye yazıldı.")

if __name__ == "__main__":
    main()

