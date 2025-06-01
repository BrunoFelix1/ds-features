package sdProject.dao;

import sdProject.models.Disciplina;
import jakarta.persistence.EntityManager;
import java.util.List;

public class DisciplinaDAO {
    private EntityManager em;

    public DisciplinaDAO(EntityManager em) {
        this.em = em;
    }

    public void salvar(Disciplina disciplina) {
        try {
            em.getTransaction().begin();
            em.persist(disciplina);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public Disciplina findById(int id) {
        return em.find(Disciplina.class, id);
    }

    public List<Disciplina> listarTodas() {
        return em.createQuery("SELECT d FROM Disciplina d ORDER BY d.nome", Disciplina.class)
                 .getResultList();
    }

    public boolean atualizar(Disciplina disciplina) {
        try {
            em.getTransaction().begin();
            em.merge(disciplina);
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
            Disciplina disciplina = em.find(Disciplina.class, id);
            if (disciplina != null) {
                em.remove(disciplina);
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