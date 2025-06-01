package sdProject.dao;

import sdProject.models.Matricula;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class MatriculaDAO {
    private EntityManager em;

    public MatriculaDAO(EntityManager em) {
        this.em = em;
    }

    public void salvar(Matricula matricula) {
        try {
            em.getTransaction().begin();
            em.persist(matricula);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public Matricula findById(int id) {
        return em.find(Matricula.class, id);
    }

    public List<Matricula> findByAluno(int alunoId) {
        TypedQuery<Matricula> query = em.createQuery(
            "SELECT m FROM Matricula m WHERE m.alunoId = :alunoId", 
            Matricula.class);
        query.setParameter("alunoId", alunoId);
        return query.getResultList();
    }

    public List<Matricula> findByDisciplina(int disciplinaId) {
        TypedQuery<Matricula> query = em.createQuery(
            "SELECT m FROM Matricula m WHERE m.disciplinaId = :disciplinaId", 
            Matricula.class);
        query.setParameter("disciplinaId", disciplinaId);
        return query.getResultList();
    }

    public Matricula findByAlunoEDisciplina(int alunoId, int disciplinaId) {
        TypedQuery<Matricula> query = em.createQuery(
            "SELECT m FROM Matricula m WHERE m.alunoId = :alunoId AND m.disciplinaId = :disciplinaId", 
            Matricula.class);
        query.setParameter("alunoId", alunoId);
        query.setParameter("disciplinaId", disciplinaId);
        List<Matricula> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Matricula> listarTodos() {
        return em.createQuery("SELECT m FROM Matricula m", Matricula.class)
                 .getResultList();
    }

    public boolean atualizar(Matricula matricula) {
        try {
            em.getTransaction().begin();
            em.merge(matricula);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public boolean atualizarNota(int alunoId, int disciplinaId, Double nota) {
        try {
            em.getTransaction().begin();
            Matricula matricula = findByAlunoEDisciplina(alunoId, disciplinaId);
            if (matricula != null) {
                matricula.setNota(nota);
                em.merge(matricula);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public boolean delete(int id) {
        try {
            em.getTransaction().begin();
            Matricula matricula = em.find(Matricula.class, id);
            if (matricula != null) {
                em.remove(matricula);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
