package sdProject.services.impl;

import sdProject.config.JPAUtil;
import sdProject.dao.AlunoDAO;
import sdProject.dao.DisciplinaDAO;
import sdProject.dao.MatriculaDAO;
import sdProject.models.Aluno;
import sdProject.models.Disciplina;
import sdProject.models.Matricula;
import sdProject.services.interfaces.HistoricoService;
import sdProject.services.interfaces.NotaService;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricoServiceImpl implements HistoricoService {
    
    private final MatriculaDAO matriculaDAO;
    private final DisciplinaDAO disciplinaDAO;
    private final AlunoDAO alunoDAO;
    private final NotaService notaService;
    private final EntityManager em;
    
    public HistoricoServiceImpl() {
        this.em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.matriculaDAO = new MatriculaDAO(em);
        this.disciplinaDAO = new DisciplinaDAO(em);
        this.alunoDAO = new AlunoDAO(em);
        try {
            this.notaService = new NotaServiceImpl();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar NotaService", e);
        }
    }
    
    @Override
    public Map<Disciplina, Double> gerarHistoricoCompleto(int alunoId) {
        try {
            // Verifica se o aluno existe
            Aluno aluno = alunoDAO.findById(alunoId);
            if (aluno == null) {
                throw new IllegalArgumentException("Aluno não encontrado");
            }
            
            Map<Disciplina, Double> historico = new HashMap<>();
            List<Matricula> matriculas = matriculaDAO.findByAluno(alunoId);
            
            for (Matricula matricula : matriculas) {
                Disciplina disciplina = disciplinaDAO.findById(matricula.getDisciplinaId());
                historico.put(disciplina, matricula.getNota());
            }
            
            return historico;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar histórico: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<Disciplina, Double> listarDisciplinasAprovadas(int alunoId) {
        try {
            Map<Disciplina, Double> disciplinasAprovadas = new HashMap<>();
            Map<Disciplina, Double> historico = gerarHistoricoCompleto(alunoId);
            
            for (Map.Entry<Disciplina, Double> entry : historico.entrySet()) {
                if (entry.getValue() != null && entry.getValue() >= 7.0) {
                    disciplinasAprovadas.put(entry.getKey(), entry.getValue());
                }
            }
            
            return disciplinasAprovadas;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar disciplinas aprovadas: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<Disciplina, Double> listarDisciplinasReprovadas(int alunoId) {
        try {
            Map<Disciplina, Double> disciplinasReprovadas = new HashMap<>();
            Map<Disciplina, Double> historico = gerarHistoricoCompleto(alunoId);
            
            for (Map.Entry<Disciplina, Double> entry : historico.entrySet()) {
                if (entry.getValue() != null && entry.getValue() < 7.0) {
                    disciplinasReprovadas.put(entry.getKey(), entry.getValue());
                }
            }
            
            return disciplinasReprovadas;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar disciplinas reprovadas: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Disciplina> listarDisciplinasEmCurso(int alunoId) {
        try {
            List<Disciplina> disciplinasEmCurso = new ArrayList<>();
            Map<Disciplina, Double> historico = gerarHistoricoCompleto(alunoId);
            
            for (Map.Entry<Disciplina, Double> entry : historico.entrySet()) {
                if (entry.getValue() == null) {
                    disciplinasEmCurso.add(entry.getKey());
                }
            }
            
            return disciplinasEmCurso;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar disciplinas em curso: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Double calcularCoeficienteRendimento(int alunoId) {
        try {
            return notaService.calcularMediaAluno(alunoId);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular coeficiente: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (notaService instanceof AutoCloseable) {
            ((AutoCloseable) notaService).close();
        }
    }
}
