package sdProject.services.impl;

import sdProject.config.DatabaseConnection;
import sdProject.dao.MatriculaDAO;
import sdProject.models.Matricula;
import sdProject.services.MatriculaService;
import sdProject.services.NotaService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class NotaServiceImpl implements NotaService {
    
    private final MatriculaDAO matriculaDAO;
    private final MatriculaService matriculaService;
    
    public NotaServiceImpl() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        this.matriculaDAO = new MatriculaDAO(conn);
        this.matriculaService = new MatriculaServiceImpl();
    }
    
    @Override
    public boolean registrarNota(int alunoId, int disciplinaId, Double nota) throws SQLException, IllegalArgumentException {
        if (!matriculaService.verificarMatricula(alunoId, disciplinaId)) {
            throw new IllegalArgumentException("Aluno não matriculado na disciplina");
        }
        if (nota != null && (nota < 0 || nota > 10)) {
            throw new IllegalArgumentException("Nota inválida. Deve estar entre 0 e 10");
        }
        
        return matriculaDAO.atualizarNota(alunoId, disciplinaId, nota);
    }
    
    @Override
    public Double consultarNota(int alunoId, int disciplinaId) throws SQLException, IllegalArgumentException {
        if (!matriculaService.verificarMatricula(alunoId, disciplinaId)) {
            throw new IllegalArgumentException("Aluno não matriculado na disciplina");
        }
        
        List<Matricula> matriculas = matriculaDAO.findByAlunoId(alunoId);
        
        for (Matricula matricula : matriculas) {
            if (matricula.getDisciplinaId() == disciplinaId) {
                return matricula.getNota();
            }
        }
        
        return null;
    }
    
    @Override
    public Double calcularMediaAluno(int alunoId) throws SQLException {
        List<Matricula> matriculas = matriculaDAO.findByAlunoId(alunoId);
        
        if (matriculas.isEmpty()) {
            return 0.0;
        }
        
        double somaNotas = 0.0;
        int contadorNotas = 0;
        
        for (Matricula matricula : matriculas) {
            if (matricula.getNota() != null) {
                somaNotas += matricula.getNota();
                contadorNotas++;
            }
        }
        
        if (contadorNotas == 0) {
            return 0.0; // Sem notas registradas
        }
        
        return somaNotas / contadorNotas;
    }
    
    @Override
    public Double calcularMediaDisciplina(int disciplinaId) throws SQLException {
        List<Matricula> matriculas = matriculaDAO.findByDisciplinaId(disciplinaId);
        
        if (matriculas.isEmpty()) {
            return 0.0;
        }
        
        double somaNotas = 0.0;
        int contadorNotas = 0;
        
        for (Matricula matricula : matriculas) {
            if (matricula.getNota() != null) {
                somaNotas += matricula.getNota();
                contadorNotas++;
            }
        }
        
        if (contadorNotas == 0) {
            return 0.0; // Sem notas registradas
        }
        
        return somaNotas / contadorNotas;
    }
}
