-- Video notlarına video_id kolonu ekle
ALTER TABLE video_note 
ADD COLUMN IF NOT EXISTS video_id VARCHAR(255) NULL;

-- Index ekle (video_id ile arama performansı için)
CREATE INDEX IF NOT EXISTS idx_video_note_konu_video ON video_note(konu_id, video_id, user_id);
CREATE INDEX IF NOT EXISTS idx_video_note_video_id ON video_note(video_id);

COMMENT ON COLUMN video_note.video_id IS 'Video ID (konuId_videoIndex formatında veya backend video ID)';

-- Mevcut kayıtlar için videoId oluştur (opsiyonel - videoUrl'den hash oluştur)
-- Bu, geriye dönük uyumluluk için yapılabilir
-- UPDATE video_note 
-- SET video_id = CONCAT(konu_id::text, '_', MD5(video_url))
-- WHERE video_id IS NULL;



