package sdProject.services.impl;

import sdProject.config.DatabaseConnection;
import sdProject.dao.AlunoDAO;
import sdProject.dao.DisciplinaDAO;
import sdProject.dao.MatriculaDAO;
import sdProject.models.Aluno;
import sdProject.models.Disciplina;
import sdProject.models.Matricula;
import sdProject.services.MatriculaService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MatriculaServiceImpl implements MatriculaService {
    
    private final MatriculaDAO matriculaDAO;
    private final AlunoDAO alunoDAO;
    private final DisciplinaDAO disciplinaDAO;
    
    public MatriculaServiceImpl() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        this.matriculaDAO = new MatriculaDAO(conn);
        this.alunoDAO = new AlunoDAO(conn);
        this.disciplinaDAO = new DisciplinaDAO(conn);
    }
    
    @Override
    public Matricula matricularAluno(int alunoId, int disciplinaId) throws SQLException, IllegalArgumentException {
        Aluno aluno = alunoDAO.findById(alunoId);
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não encontrado");
        }
        
        if (!aluno.isAtivo()) {
            throw new IllegalArgumentException("Aluno inativo não pode ser matriculado");
        }
        
        Disciplina disciplina = disciplinaDAO.findById(disciplinaId);
        if (disciplina == null) {
            throw new IllegalArgumentException("Disciplina não encontrada");
        }
        
        if (verificarMatricula(alunoId, disciplinaId)) {
            throw new IllegalArgumentException("Aluno já matriculado na disciplina");
        }
        
        Matricula matricula = new Matricula();
        matricula.setAlunoId(alunoId);
        matricula.setDisciplinaId(disciplinaId);
        matriculaDAO.salvar(matricula);
        
        return matricula;
    }
    
    @Override
    public boolean cancelarMatricula(int alunoId, int disciplinaId) throws SQLException {
        List<Matricula> matriculas = matriculaDAO.findByAlunoId(alunoId);
        
        for (Matricula matricula : matriculas) {
            if (matricula.getDisciplinaId() == disciplinaId) {
                return matriculaDAO.delete(matricula.getId());
            }
        }
        
        return false;
    }
    
    @Override
    public List<Matricula> buscarMatriculasPorAluno(int alunoId) throws SQLException {
        return matriculaDAO.findByAlunoId(alunoId);
    }
    
    @Override
    public List<Matricula> buscarMatriculasPorDisciplina(int disciplinaId) throws SQLException {
        return matriculaDAO.findByDisciplinaId(disciplinaId);
    }
    
    @Override
    public boolean verificarMatricula(int alunoId, int disciplinaId) throws SQLException {
        List<Matricula> matriculasAluno = matriculaDAO.findByAlunoId(alunoId);
        
        for (Matricula matricula : matriculasAluno) {
            if (matricula.getDisciplinaId() == disciplinaId) {
                return true;
            }
        }
        
        return false;
    }
}
