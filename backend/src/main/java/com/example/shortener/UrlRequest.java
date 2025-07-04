import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UrlRequest {
    @NotBlank
    @Pattern(regexp = "^(http|https)://.*$", message = "Invalid URL format")
    private String url;

    // getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}