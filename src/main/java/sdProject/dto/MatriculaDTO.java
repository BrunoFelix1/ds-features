package sdProject.dto;

/**
 * DTO para operações relacionadas a matrículas
 */
public class MatriculaDTO {
    private Integer id;
    private Integer alunoId;
    private Integer disciplinaId;
    private Double nota;
    
    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getAlunoId() { return alunoId; }
    public void setAlunoId(Integer alunoId) { this.alunoId = alunoId; }
    
    public Integer getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(Integer disciplinaId) { this.disciplinaId = disciplinaId; }
    
    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }
}
