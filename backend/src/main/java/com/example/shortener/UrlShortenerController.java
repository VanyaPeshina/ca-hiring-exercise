import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.util.logging.Logger;

/**
What this implementation covers:
       - REST endpoints: POST /api/shorten and GET /{shortCode}
       - CORS config for http://localhost:3000
       - URL validation using regex
       - In-memory ConcurrentHashMap store (thread-safe variant of HashMap)
       - Redirect handling via RedirectView
       - Logs on each important operation

 Note: ConcurrentHashMap allows concurrent read and write operations
 from multiple threads withoutlocking the entire map.

 It divides the map into segments internally and locks only the segment being modified,
 making it far more scalable and performant in multi-threaded applications
 (like a web server handling multiple concurrent requests).

 The ConcurrentHashMap safely handles multiple incoming requests to add and retrieve short URLs concurrently,
 without race conditions or synchronization issues.
*/

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class UrlShortenerController {

    private final Map<String, String> db = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final Logger logger = Logger.getLogger("UrlShortener");

    public UrlShortenerController() {
        db.put("abc123", "https://example.com");
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody UrlRequest request) {
        String shortCode = generateShortCode();
        db.put(shortCode, request.getUrl());
        logger.info("Stored mapping: " + shortCode + " -> " + request.getUrl());

        return ResponseEntity.ok(Map.of(
                "short_code", shortCode,
                "short_url", "http://localhost:5000/" + shortCode,
                "original_url", request.getUrl()
        ));
    }

    @GetMapping("/{shortCode}")
    public Object redirect(@PathVariable String shortCode) {
        logger.info("Received redirect request for short code: " + shortCode);

        if (db.containsKey(shortCode)) {
            logger.info("Redirecting to " + db.get(shortCode));
            return new RedirectView(db.get(shortCode));
        } else {
            logger.warning("Short code " + shortCode + " not found");
            return ResponseEntity.status(404).body(Map.of("detail", "Short code not found"));
        }
    }

    private String generateShortCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code;
        do {
            code = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        } while (db.containsKey(code.toString()));
        return code.toString();
    }
}