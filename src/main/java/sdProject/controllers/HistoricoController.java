package sdProject.controllers;

import sdProject.models.Disciplina;
import sdProject.services.interfaces.HistoricoService;
import sdProject.services.impl.HistoricoServiceImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HistoricoController {
    
    private final HistoricoService historicoService;
    
    public HistoricoController() throws SQLException {
        this.historicoService = new HistoricoServiceImpl();
    }
    
    public Map<String, Object> gerarHistoricoCompleto(int alunoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<Disciplina, Double> historico = historicoService.gerarHistoricoCompleto(alunoId);
            response.put("status", "success");
            response.put("data", converterHistoricoParaMap(historico));
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> listarDisciplinasAprovadas(int alunoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<Disciplina, Double> disciplinasAprovadas = historicoService.listarDisciplinasAprovadas(alunoId);
            response.put("status", "success");
            response.put("data", converterHistoricoParaMap(disciplinasAprovadas));
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> listarDisciplinasReprovadas(int alunoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<Disciplina, Double> disciplinasReprovadas = historicoService.listarDisciplinasReprovadas(alunoId);
            response.put("status", "success");
            response.put("data", converterHistoricoParaMap(disciplinasReprovadas));
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> listarDisciplinasEmCurso(int alunoId) {

        Map<String, Object> response = new HashMap<>(); 
        try {
            List<Disciplina> disciplinasEmCurso = historicoService.listarDisciplinasEmCurso(alunoId);
            response.put("status", "success");
            response.put("data", disciplinasEmCurso);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;

    }
    

    private List<Map<String, Object>> converterHistoricoParaMap(Map<Disciplina, Double> historico) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Map.Entry<Disciplina, Double> entry : historico.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("disciplinaNome", entry.getKey().getNome());
            item.put("nota", entry.getValue());
            lista.add(item);
        }
        return lista;
    }

}
