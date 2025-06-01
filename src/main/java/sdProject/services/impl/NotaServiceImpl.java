package sdProject.services.impl;

import sdProject.config.JPAUtil;
import sdProject.dao.MatriculaDAO;
import sdProject.models.Matricula;
import sdProject.services.interfaces.MatriculaService;
import sdProject.services.interfaces.NotaService;

import jakarta.persistence.EntityManager;

import java.util.List;

public class NotaServiceImpl implements NotaService {
    
    private final MatriculaDAO matriculaDAO;
    private final MatriculaService matriculaService;
    private final EntityManager em;
    
    public NotaServiceImpl() {
        this.em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.matriculaDAO = new MatriculaDAO(em);
        try {
            this.matriculaService = new MatriculaServiceImpl();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar MatriculaService", e);
        }
    }
    
    @Override
    public boolean registrarNota(int alunoId, int disciplinaId, Double nota) {
        try {
            if (!matriculaService.verificarMatricula(alunoId, disciplinaId)) {
                throw new IllegalArgumentException("Aluno não matriculado na disciplina");
            }
            if (nota != null && (nota < 0 || nota > 10)) {
                throw new IllegalArgumentException("Nota inválida. Deve estar entre 0 e 10");
            }
            
            em.getTransaction().begin();
            boolean updated = matriculaDAO.atualizarNota(alunoId, disciplinaId, nota);
            em.getTransaction().commit();
            return updated;
        } catch (IllegalArgumentException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erro ao registrar nota: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Double consultarNota(int alunoId, int disciplinaId) {
        try {
            if (!matriculaService.verificarMatricula(alunoId, disciplinaId)) {
                throw new IllegalArgumentException("Aluno não matriculado na disciplina");
            }
            
            List<Matricula> matriculas = matriculaDAO.findByAluno(alunoId);
            
            for (Matricula matricula : matriculas) {
                if (matricula.getDisciplinaId() == disciplinaId) {
                    return matricula.getNota();
                }
            }
            
            return null;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar nota: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Double calcularMediaAluno(int alunoId) {
        try {
            List<Matricula> matriculas = matriculaDAO.findByAluno(alunoId);
            
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
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular média do aluno: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Double calcularMediaDisciplina(int disciplinaId) {
        try {
            List<Matricula> matriculas = matriculaDAO.findByDisciplina(disciplinaId);
            
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
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular média da disciplina: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (matriculaService instanceof AutoCloseable) {
            ((AutoCloseable) matriculaService).close();
        }
    }
}
