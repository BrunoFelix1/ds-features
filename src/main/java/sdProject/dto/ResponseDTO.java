package sdProject.dto;

/**
 * Classe padrão para respostas das requisições
 * @param <T> Tipo do dado retornado
 */
public class ResponseDTO<T> {
    private String status;
    private String message;
    private T data;
    
    public ResponseDTO() {}
    
    public ResponseDTO(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    
    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>("success", null, data);
    }
    
    public static <T> ResponseDTO<T> success(String message, T data) {
        return new ResponseDTO<>("success", message, data);
    }
    
    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>("error", message, null);
    }
    
    // Getters e Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
