package sdProject.services.impl;

import sdProject.config.JPAUtil;
import sdProject.dao.AlunoDAO;
import sdProject.dao.DisciplinaDAO;
import sdProject.dao.MatriculaDAO;
import sdProject.models.Aluno;
import sdProject.models.Disciplina;
import sdProject.models.Matricula;
import sdProject.services.interfaces.MatriculaService;

import jakarta.persistence.EntityManager;

import java.util.List;

public class MatriculaServiceImpl implements MatriculaService {
    
    private final MatriculaDAO matriculaDAO;
    private final AlunoDAO alunoDAO;
    private final DisciplinaDAO disciplinaDAO;
    private final EntityManager em;
    
    public MatriculaServiceImpl() {
        this.em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.matriculaDAO = new MatriculaDAO(em);
        this.alunoDAO = new AlunoDAO(em);
        this.disciplinaDAO = new DisciplinaDAO(em);
    }
    
    @Override
    public Matricula matricularAluno(int alunoId, int disciplinaId) {
        try {
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
            
            em.getTransaction().begin();
            matriculaDAO.salvar(matricula);
            em.getTransaction().commit();
            
            return matricula;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erro ao matricular aluno: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean cancelarMatricula(int alunoId, int disciplinaId) {
        try {
            List<Matricula> matriculas = matriculaDAO.findByAluno(alunoId);
            
            for (Matricula matricula : matriculas) {
                if (matricula.getDisciplinaId() == disciplinaId) {
                    em.getTransaction().begin();
                    boolean deleted = matriculaDAO.delete(matricula.getId());
                    em.getTransaction().commit();
                    return deleted;
                }
            }
            
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erro ao cancelar matrícula: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Matricula> buscarMatriculasPorAluno(int alunoId) {
        try {
            return matriculaDAO.findByAluno(alunoId);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar matrículas por aluno: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Matricula> buscarMatriculasPorDisciplina(int disciplinaId) {
        try {
            return matriculaDAO.findByDisciplina(disciplinaId);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar matrículas por disciplina: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean verificarMatricula(int alunoId, int disciplinaId) {
        try {
            List<Matricula> matriculasAluno = matriculaDAO.findByAluno(alunoId);
            
            for (Matricula matricula : matriculasAluno) {
                if (matricula.getDisciplinaId() == disciplinaId) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar matrícula: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
