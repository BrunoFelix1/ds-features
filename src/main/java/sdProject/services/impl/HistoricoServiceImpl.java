package sdProject.services.impl;

import sdProject.config.DatabaseConnection;
import sdProject.dao.AlunoDAO;
import sdProject.dao.DisciplinaDAO;
import sdProject.dao.MatriculaDAO;
import sdProject.models.Aluno;
import sdProject.models.Disciplina;
import sdProject.models.Matricula;
import sdProject.services.HistoricoService;
import sdProject.services.NotaService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricoServiceImpl implements HistoricoService {
    
    private final MatriculaDAO matriculaDAO;
    private final DisciplinaDAO disciplinaDAO;
    private final AlunoDAO alunoDAO;
    private final NotaService notaService;
    
    public HistoricoServiceImpl() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        this.matriculaDAO = new MatriculaDAO(conn);
        this.disciplinaDAO = new DisciplinaDAO(conn);
        this.alunoDAO = new AlunoDAO(conn);
        this.notaService = new NotaServiceImpl();
    }
    
    @Override
    public Map<Disciplina, Double> gerarHistoricoCompleto(int alunoId) throws SQLException, IllegalArgumentException {
        // Verifica se o aluno existe
        Aluno aluno = alunoDAO.findById(alunoId);
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não encontrado");
        }
        
        Map<Disciplina, Double> historico = new HashMap<>();
        List<Matricula> matriculas = matriculaDAO.findByAlunoId(alunoId);
        
        for (Matricula matricula : matriculas) {
            Disciplina disciplina = disciplinaDAO.findById(matricula.getDisciplinaId());
            historico.put(disciplina, matricula.getNota());
        }
        
        return historico;
    }
    
    @Override
    public Map<Disciplina, Double> listarDisciplinasAprovadas(int alunoId) throws SQLException {
        Map<Disciplina, Double> disciplinasAprovadas = new HashMap<>();
        Map<Disciplina, Double> historico = gerarHistoricoCompleto(alunoId);
        
        for (Map.Entry<Disciplina, Double> entry : historico.entrySet()) {
            if (entry.getValue() != null && entry.getValue() >= 7.0) {
                disciplinasAprovadas.put(entry.getKey(), entry.getValue());
            }
        }
        
        return disciplinasAprovadas;
    }
    
    @Override
    public Map<Disciplina, Double> listarDisciplinasReprovadas(int alunoId) throws SQLException {
        Map<Disciplina, Double> disciplinasReprovadas = new HashMap<>();
        Map<Disciplina, Double> historico = gerarHistoricoCompleto(alunoId);
        
        for (Map.Entry<Disciplina, Double> entry : historico.entrySet()) {
            if (entry.getValue() != null && entry.getValue() < 7.0) {
                disciplinasReprovadas.put(entry.getKey(), entry.getValue());
            }
        }
        
        return disciplinasReprovadas;
    }
    
    @Override
    public List<Disciplina> listarDisciplinasEmCurso(int alunoId) throws SQLException {
        List<Disciplina> disciplinasEmCurso = new ArrayList<>();
        Map<Disciplina, Double> historico = gerarHistoricoCompleto(alunoId);
        
        for (Map.Entry<Disciplina, Double> entry : historico.entrySet()) {
            if (entry.getValue() == null) {
                disciplinasEmCurso.add(entry.getKey());
            }
        }
        
        return disciplinasEmCurso;
    }
    
    @Override
    public Double calcularCoeficienteRendimento(int alunoId) throws SQLException {
        return notaService.calcularMediaAluno(alunoId);
    }
}
