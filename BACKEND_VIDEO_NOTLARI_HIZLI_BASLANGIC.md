# Backend Video Notları - Hızlı Başlangıç Kılavuzu

## 🎯 Özet

Frontend'den gelen video notları artık `videoId` ile ayrılıyor. Her video için ayrı notlar tutulması gerekiyor.

---

## 1. Database Migration (İLK ADIM)

```sql
-- video_note tablosuna video_id kolonu ekle
ALTER TABLE video_note 
ADD COLUMN IF NOT EXISTS video_id VARCHAR(255) NULL;

-- Index ekle (performans için)
CREATE INDEX IF NOT EXISTS idx_video_note_konu_video 
ON video_note(konu_id, video_id, user_id);
```

**⚠️ ÖNEMLİ:** 
- Migration dosyası: `V44__add_video_id_to_video_note.sql` (zaten mevcut)
- Migration'ı çalıştırdıktan sonra backend'i yeniden başlatın.

---

## 2. Frontend'den Gelen Request Formatı

### POST /api/video-notes

```json
{
  "konuId": 123,
  "videoId": "123_0",           // Opsiyonel: "konuId_videoIndex" formatında
  "videoUrl": "https://www.youtube.com/watch?v=...",
  "noteText": "Bu önemli bir not",
  "timestampSeconds": 120
}
```

**ÖNEMLİ:**
- `videoId` **opsiyoneldir** (null veya undefined olabilir)
- Eğer `videoId` yoksa veya `videoUrl` ile **aynıysa**, sadece `videoUrl` kullanılır
- `videoId` formatı: `"konuId_videoIndex"` (örn: "123_0", "123_1", "123_2")
- Backend'de `videoId` sadece `videoUrl`'den **farklıysa** kaydedilir

### GET /api/video-notes

```
GET /api/video-notes?konuId=123&videoId=123_0&videoUrl=https://...
```

**ÖNEMLİ:**
- `videoId` varsa ve `videoUrl`'den farklıysa → Sadece o `videoId`'ye ait notlar döndürülür
- `videoId` yoksa veya `videoUrl` ile aynıysa ama `videoUrl` varsa → `videoUrl`'e göre filtreleme yapılır
- İkisi de yoksa → Sadece `konuId`'ye göre filtreleme yapılır (tüm videolar)

---

## 3. Java Spring Boot - Mevcut Implementasyon

### Entity (VideoNote.java)

```java
@Entity
@Table(name = "video_note")  // ⚠️ Tablo adı: video_note (tekil)
public class VideoNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "konu_id", nullable = false)
    private Long konuId;
    
    @Column(name = "video_id", length = 255)  // NULL olabilir
    private String videoId;
    
    @Column(name = "video_url", nullable = false, columnDefinition = "TEXT")
    private String videoUrl;
    
    @Column(name = "note_text", nullable = false, columnDefinition = "TEXT")
    private String noteText;
    
    @Column(name = "timestamp_seconds", nullable = false)
    private Integer timestampSeconds;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // ... getters, setters
}
```

### Repository (VideoNoteRepository.java)

```java
@Repository
public interface VideoNoteRepository extends JpaRepository<VideoNote, Long> {
    
    // videoId'ye göre filtrele
    List<VideoNote> findByKonuIdAndVideoIdAndUserIdOrderByTimestampSecondsAsc(
        Long konuId, String videoId, Long userId
    );
    
    // videoUrl'e göre filtrele (geriye dönük uyumluluk)
    List<VideoNote> findByKonuIdAndVideoUrlAndUserIdOrderByTimestampSecondsAsc(
        Long konuId, String videoUrl, Long userId
    );
    
    // Sadece konuId'ye göre filtrele
    List<VideoNote> findByUserIdAndKonuIdOrderByTimestampSecondsAsc(
        Long userId, Long konuId
    );
    
    // Sadece videoUrl'e göre filtrele
    List<VideoNote> findByUserIdAndVideoUrlOrderByTimestampSecondsAsc(
        Long userId, String videoUrl
    );
    
    // Tüm notlar
    List<VideoNote> findByUserIdOrderByTimestampSecondsAsc(Long userId);
}
```

### Controller (VideoNoteController.java)

**Not Ekleme (POST):**
```java
@PostMapping
@PreAuthorize("isAuthenticated()")
public ResponseEntity<?> createVideoNote(
    @Valid @RequestBody CreateVideoNoteRequest request,
    Authentication authentication
) {
    // videoId varsa ve videoUrl'den farklıysa kaydet
    if (request.getVideoId() != null && !request.getVideoId().trim().isEmpty() 
        && !request.getVideoId().equals(request.getVideoUrl())) {
        note.setVideoId(request.getVideoId().trim());
    } else {
        note.setVideoId(null);  // videoUrl kullanılacak
    }
    
    note.setVideoUrl(request.getVideoUrl());
    // ... diğer alanlar
}
```

**Not Listeleme (GET):**
```java
@GetMapping
@PreAuthorize("isAuthenticated()")
public ResponseEntity<VideoNotesResponse> getVideoNotes(
    @RequestParam(required = false) Long konuId,
    @RequestParam(required = false) String videoId,
    @RequestParam(required = false) String videoUrl,
    Authentication authentication
) {
    // Öncelik: videoId varsa ve videoUrl'den farklıysa
    if (konuId != null && videoId != null && !videoId.trim().isEmpty() 
        && !videoId.equals(videoUrl)) {
        notes = repository.findByKonuIdAndVideoIdAndUserIdOrderByTimestampSecondsAsc(
            konuId, videoId.trim(), userId
        );
    }
    // videoId yoksa veya videoUrl ile aynıysa
    else if (konuId != null && videoUrl != null && !videoUrl.trim().isEmpty()) {
        notes = repository.findByKonuIdAndVideoUrlAndUserIdOrderByTimestampSecondsAsc(
            konuId, videoUrl.trim(), userId
        );
    }
    // Sadece konuId
    else if (konuId != null) {
        notes = repository.findByUserIdAndKonuIdOrderByTimestampSecondsAsc(userId, konuId);
    }
    // ...
}
```

### DTO'lar

**CreateVideoNoteRequest.java:**
```java
public class CreateVideoNoteRequest {
    @NotNull(message = "Konu ID is required")
    private Long konuId;
    
    private String videoId;  // Opsiyonel
    
    @NotBlank(message = "Video URL is required")
    private String videoUrl;
    
    @NotBlank(message = "Note text is required")
    private String noteText;
    
    @NotNull(message = "Timestamp seconds is required")
    @Min(value = 0)
    private Integer timestampSeconds;
    
    // getters, setters
}
```

**VideoNoteResponse.java:**
```java
public class VideoNoteResponse {
    private Long id;
    private Long userId;
    private Long konuId;
    private String videoId;  // Opsiyonel
    private String videoUrl;
    private String noteText;
    private Integer timestampSeconds;
    private String timestampFormatted;  // "2:00" formatında
    private Instant createdAt;
    private Instant updatedAt;
    
    // getters, setters
}
```

---

## 4. Filtreleme Mantığı (Öncelik Sırası)

1. **videoId varsa ve videoUrl'den farklıysa** → `videoId` ile filtrele
2. **videoId yoksa veya videoUrl ile aynıysa** → `videoUrl` ile filtrele
3. **Sadece konuId varsa** → `konuId` ile filtrele (tüm videolar)
4. **Hiçbiri yoksa** → Tüm notlar

---

## 5. Test Senaryoları

### Test 1: Not Ekleme (videoId ile)

```bash
curl -X POST http://localhost:8080/api/video-notes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "konuId": 123,
    "videoId": "123_0",
    "videoUrl": "https://www.youtube.com/watch?v=abc123",
    "noteText": "Test notu",
    "timestampSeconds": 60
  }'
```

**Beklenen:** 
- Not kaydedilmeli
- Response'da `videoId: "123_0"` görünmeli
- Database'de `video_id = "123_0"` kaydedilmeli

### Test 2: Not Ekleme (videoId == videoUrl)

```bash
curl -X POST http://localhost:8080/api/video-notes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "konuId": 123,
    "videoId": "https://www.youtube.com/watch?v=abc123",
    "videoUrl": "https://www.youtube.com/watch?v=abc123",
    "noteText": "Test notu",
    "timestampSeconds": 60
  }'
```

**Beklenen:**
- Not kaydedilmeli
- Response'da `videoId: null` görünmeli (videoUrl ile aynı olduğu için)
- Database'de `video_id = NULL` kaydedilmeli

### Test 3: Notları Yükleme (videoId ile)

```bash
curl -X GET "http://localhost:8080/api/video-notes?konuId=123&videoId=123_0" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Beklenen:** Sadece `videoId: "123_0"` olan notlar dönmeli

### Test 4: Notları Yükleme (videoUrl ile - geriye dönük)

```bash
curl -X GET "http://localhost:8080/api/video-notes?konuId=123&videoUrl=https://www.youtube.com/watch?v=abc123" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Beklenen:** Sadece `videoUrl` eşleşen notlar dönmeli

---

## 6. Önemli Notlar

1. **Tablo Adı:** `video_note` (tekil, çoğul değil!)
2. **videoId Opsiyonel:** Eğer `videoId` yoksa veya `videoUrl` ile aynıysa, sadece `videoUrl` kullanılır
3. **Geriye Dönük Uyumluluk:** Eski notlar `videoUrl`'e göre çalışmaya devam eder
4. **User ID:** Her not kullanıcıya özel olmalı (JWT token'dan alınır)
5. **NULL Handling:** `video_id` kolonu NULL olabilir (eski notlar için)
6. **Index:** Performans için `(konu_id, video_id, user_id)` index'i mevcut

---

## 7. Response Formatı

### GET /api/video-notes Response

```json
{
  "notes": [
    {
      "id": 1,
      "userId": 1,
      "konuId": 123,
      "videoId": "123_0",
      "videoUrl": "https://www.youtube.com/watch?v=abc123",
      "noteText": "Bu önemli bir not",
      "timestampSeconds": 120,
      "timestampFormatted": "2:00",
      "createdAt": "2024-01-01T12:00:00Z",
      "updatedAt": "2024-01-01T12:00:00Z"
    }
  ]
}
```

### POST /api/video-notes Response

```json
{
  "id": 1,
  "userId": 1,
  "konuId": 123,
  "videoId": "123_0",
  "videoUrl": "https://www.youtube.com/watch?v=abc123",
  "noteText": "Bu önemli bir not",
  "timestampSeconds": 120,
  "timestampFormatted": "2:00",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

---

## 8. Hata Ayıklama (Debugging)

### Backend Log'ları

Controller'da detaylı log'lar mevcut:
- Not ekleme isteği detayları
- Filtreleme yöntemi
- Bulunan not sayısı
- Veritabanı doğrulaması

### Debug Endpoint

```bash
GET /api/video-notes/debug/all
```

Kullanıcının tüm notlarını getirir (videoId'ye bakmadan).

### Database Kontrolü

```sql
-- Tüm notları kontrol et
SELECT * FROM video_note ORDER BY created_at DESC LIMIT 10;

-- video_id kolonunu kontrol et
SELECT id, konu_id, video_id, video_url, note_text, user_id 
FROM video_note 
WHERE konu_id = 123;

-- videoId'ye göre filtrele
SELECT * FROM video_note 
WHERE konu_id = 123 AND video_id = '123_0' AND user_id = 1;
```

---

## 9. Checklist

- [x] Database migration çalıştırıldı (`video_id` kolonu eklendi - V44)
- [x] Entity güncellendi (`videoId` alanı eklendi)
- [x] Repository metodları eklendi
- [x] Controller güncellendi (filtreleme mantığı)
- [x] DTO'lar güncellendi
- [x] Debug log'ları eklendi
- [ ] Test edildi (not ekleme ve yükleme)
- [ ] Frontend entegrasyonu tamamlandı

---

## 10. Sorun Giderme

### Notlar eklenmiyor
1. ✅ Database'de `video_id` kolonu var mı kontrol edin
2. ✅ Backend log'larını kontrol edin (console'da detaylı log'lar var)
3. ✅ Frontend'den gönderilen request body'yi kontrol edin (Network tab)
4. ✅ `userId` doğru alınıyor mu kontrol edin (JWT token)
5. ✅ `videoId` ve `videoUrl` değerlerini kontrol edin

### Notlar yüklenmiyor
1. ✅ Query parametreleri doğru geliyor mu kontrol edin
2. ✅ Database'de notlar var mı kontrol edin
3. ✅ `videoId` ve `videoUrl` filtrelemesi doğru çalışıyor mu kontrol edin
4. ✅ `videoId == videoUrl` durumunda `videoUrl` ile filtreleme yapılıyor mu?

---

## 11. Mevcut Dosyalar

- **Entity:** `src/main/java/com/example/backend/model/VideoNote.java`
- **Repository:** `src/main/java/com/example/backend/repository/VideoNoteRepository.java`
- **Controller:** `src/main/java/com/example/backend/controller/VideoNoteController.java`
- **DTO'lar:** 
  - `src/main/java/com/example/backend/dto/CreateVideoNoteRequest.java`
  - `src/main/java/com/example/backend/dto/VideoNoteResponse.java`
  - `src/main/java/com/example/backend/dto/VideoNotesResponse.java`
- **Migration:** `src/main/resources/db/migration/V44__add_video_id_to_video_note.sql`

---

**Son Güncelleme:** Backend implementasyonu tamamlandı ve test edilmeye hazır.


