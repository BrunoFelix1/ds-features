package sdProject.dto;

import sdProject.models.Disciplina;
import java.util.List;
import java.util.Map;

/**
 * DTO para operações relacionadas ao histórico acadêmico
 */
public class HistoricoDTO {
    private Integer alunoId;
    private Map<Disciplina, Double> historico;
    private List<Disciplina> disciplinasEmCurso;
    private Double coeficienteRendimento;
    
    // Getters e Setters
    public Integer getAlunoId() { return alunoId; }
    public void setAlunoId(Integer alunoId) { this.alunoId = alunoId; }
    
    public Map<Disciplina, Double> getHistorico() { return historico; }
    public void setHistorico(Map<Disciplina, Double> historico) { this.historico = historico; }
    
    public List<Disciplina> getDisciplinasEmCurso() { return disciplinasEmCurso; }
    public void setDisciplinasEmCurso(List<Disciplina> disciplinasEmCurso) { this.disciplinasEmCurso = disciplinasEmCurso; }
    
    public Double getCoeficienteRendimento() { return coeficienteRendimento; }
    public void setCoeficienteRendimento(Double coeficienteRendimento) { this.coeficienteRendimento = coeficienteRendimento; }
}
