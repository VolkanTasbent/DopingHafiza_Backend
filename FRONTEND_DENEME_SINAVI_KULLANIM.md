# 📝 Frontend - Deneme Sınavı Kullanım Kılavuzu

## 🎯 Problem: Mevcut Denemelere Soru Ekleyememe

**Sorun:** Her soru eklediğinizde yeni deneme oluşturuluyor, mevcut denemeye eklenemiyor.

**Çözüm:** Frontend'de mevcut denemeleri listeleyip seçmek gerekiyor.

---

## 🔌 API Endpoint'leri

### 1. Tüm Denemeleri Listele (Frontend Format)

```javascript
GET /api/deneme-sinavi
```

**Response:**
```json
[
  {
    "id": 1,
    "adi": "TYT Deneme 1",
    "kategori": "TYT",
    "olusturmaTarihi": "2025-11-03T14:44:00Z",
    "aciklama": null,
    "soruSayisi": 5
  },
  {
    "id": 2,
    "adi": "AYT Deneme 1",
    "kategori": "AYT",
    "olusturmaTarihi": "2025-11-03T14:45:00Z",
    "aciklama": null,
    "soruSayisi": 10
  }
]
```

### 2. Tek Deneme Detayı

```javascript
GET /api/deneme-sinavi/{id}
```

**Response:**
```json
{
  "id": 1,
  "adi": "TYT Deneme 1",
  "kategori": "TYT",
  "olusturmaTarihi": "2025-11-03T14:44:00Z",
  "aciklama": null,
  "soruSayisi": 5
}
```

### 3. Yeni Deneme Oluştur

```javascript
POST /api/deneme-sinavi
Content-Type: application/json
Authorization: Bearer <token>

{
  "adi": "TYT Deneme 2",
  "kategori": "TYT"
}
```

**Response:**
```json
{
  "id": 3,
  "adi": "TYT Deneme 2",
  "kategori": "TYT",
  "olusturmaTarihi": "2025-11-03T15:00:00Z",
  "aciklama": null,
  "soruSayisi": 0
}
```

### 4. Mevcut Denemeye Soru Ekle

```javascript
POST /api/deneme-sinavlari/{denemeId}/sorular
Content-Type: application/json
Authorization: Bearer <token>

{
  "soruMetni": "2 + 2 kaçtır?",
  "sikA": "3",
  "sikB": "4",
  "sikC": "5",
  "sikD": "6",
  "dogruCevap": "B",
  "zorluk": 1,
  "konular": "Temel İşlemler",
  "dersId": 1
}
```

---

## 💻 Frontend Örnek Kod

### React Örnek:

```typescript
// 1. Mevcut denemeleri yükle
const [denemeler, setDenemeler] = useState([]);
const [seciliDeneme, setSeciliDeneme] = useState(null);

useEffect(() => {
  fetch('/api/deneme-sinavi')
    .then(res => res.json())
    .then(data => setDenemeler(data));
}, []);

// 2. Deneme seçimi dropdown
<select 
  value={seciliDeneme?.id || ''} 
  onChange={(e) => {
    const id = parseInt(e.target.value);
    const deneme = denemeler.find(d => d.id === id);
    setSeciliDeneme(deneme);
  }}
>
  <option value="">-- Deneme Seçin veya Yeni Oluşturun --</option>
  {denemeler.map(d => (
    <option key={d.id} value={d.id}>
      {d.adi} ({d.kategori}) - {d.soruSayisi} soru
    </option>
  ))}
</select>

// 3. Yeni deneme oluştur butonu
<button onClick={async () => {
  const yeniAd = prompt('Deneme adı:');
  if (!yeniAd) return;
  
  const response = await fetch('/api/deneme-sinavi', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      adi: yeniAd,
      kategori: 'TYT' // veya 'AYT'
    })
  });
  
  const yeniDeneme = await response.json();
  setSeciliDeneme(yeniDeneme);
  setDenemeler([...denemeler, yeniDeneme]);
}}>
  Yeni Deneme Oluştur
</button>

// 4. Soru ekle (seçili denemeye)
<button onClick={async () => {
  if (!seciliDeneme) {
    alert('Lütfen önce bir deneme seçin!');
    return;
  }
  
  await fetch(`/api/deneme-sinavlari/${seciliDeneme.id}/sorular`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      soruMetni: '...',
      sikA: '...',
      // ...
      dersId: selectedDersId
    })
  });
  
  // Başarılı! Deneme listesini yenile
  const response = await fetch('/api/deneme-sinavi');
  const updated = await response.json();
  setDenemeler(updated);
  
  // Seçili denemenin soru sayısını güncelle
  const updatedDeneme = updated.find(d => d.id === seciliDeneme.id);
  setSeciliDeneme(updatedDeneme);
}}>
  Soru Ekle
</button>
```

---

## ✅ Doğru Akış

1. **Sayfa Açıldığında:**
   - `GET /api/deneme-sinavi` çağrılır
   - Mevcut denemeler listeye yüklenir

2. **Deneme Seçimi:**
   - Kullanıcı dropdown'dan mevcut bir deneme seçer
   - VEYA "Yeni Deneme Oluştur" butonuna tıklar

3. **Soru Ekleme:**
   - Seçili denemenin ID'si (`seciliDeneme.id`) ile
   - `POST /api/deneme-sinavlari/{denemeId}/sorular` çağrılır
   - Soru mevcut denemeye eklenir ✅

---

## ❌ Yanlış Akış (Mevcut Sorun)

1. ❌ Her soru eklerken yeni deneme oluşturuluyor
2. ❌ Mevcut denemeler listelenmiyor
3. ❌ Kullanıcı deneme seçemiyor

---

## 🔧 Hızlı Test

```bash
# 1. Mevcut denemeleri listele
curl http://localhost:8080/api/deneme-sinavi

# 2. Yeni deneme oluştur
curl -X POST http://localhost:8080/api/deneme-sinavi \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"adi":"Test Deneme","kategori":"TYT"}'

# 3. Oluşturulan denemenin ID'sini kullanarak soru ekle
curl -X POST http://localhost:8080/api/deneme-sinavlari/1/sorular \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "soruMetni":"Test soru",
    "sikA":"A",
    "sikB":"B",
    "dogruCevap":"A",
    "dersId":1
  }'
```

---

## 📌 Önemli Notlar

1. **Deneme Seçimi Zorunlu:** Soru eklemeden önce mutlaka bir deneme seçilmeli
2. **ID Kullanımı:** Soru eklerken `denemeId` parametresi zorunlu
3. **Liste Güncelleme:** Soru eklendikten sonra deneme listesini yenileyin (soru sayısı güncellenir)


