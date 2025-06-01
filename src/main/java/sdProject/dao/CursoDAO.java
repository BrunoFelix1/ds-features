package sdProject.dao;

import sdProject.models.Curso;
import jakarta.persistence.EntityManager;
import java.util.List;

public class CursoDAO {
    private EntityManager em;

    public CursoDAO(EntityManager em) {
        this.em = em;
    }

    public void salvar(Curso curso) {
        try {
            em.getTransaction().begin();
            em.persist(curso);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public Curso findById(int id) {
        return em.find(Curso.class, id);
    }

    public List<Curso> listarTodos() {
        return em.createQuery("SELECT c FROM Curso c ORDER BY c.nome", Curso.class)
                 .getResultList();
    }

    public boolean atualizar(Curso curso) {
        try {
            em.getTransaction().begin();
            em.merge(curso);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public boolean delete(int id) {
        try {
            em.getTransaction().begin();
            Curso curso = em.find(Curso.class, id);
            if (curso != null) {
                em.remove(curso);
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