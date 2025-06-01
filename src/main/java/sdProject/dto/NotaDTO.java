package sdProject.dto;

/**
 * DTO para operações relacionadas a notas
 */
public class NotaDTO {
    private Integer alunoId;
    private Integer disciplinaId;
    private Double nota;
    private Double mediaAluno;
    private Double mediaDisciplina;
    
    // Getters e Setters
    public Integer getAlunoId() { return alunoId; }
    public void setAlunoId(Integer alunoId) { this.alunoId = alunoId; }
    
    public Integer getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(Integer disciplinaId) { this.disciplinaId = disciplinaId; }
    
    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }
    
    public Double getMediaAluno() { return mediaAluno; }
    public void setMediaAluno(Double mediaAluno) { this.mediaAluno = mediaAluno; }
    
    public Double getMediaDisciplina() { return mediaDisciplina; }
    public void setMediaDisciplina(Double mediaDisciplina) { this.mediaDisciplina = mediaDisciplina; }
}
