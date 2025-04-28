package sdProject.controllers;

import sdProject.services.interfaces.NotaService;
import sdProject.services.impl.NotaServiceImpl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class NotaController {
    
    private final NotaService notaService;
    
    public NotaController() throws SQLException {
        this.notaService = new NotaServiceImpl();
    }
    
    public Map<String, Object> registrarNota(int alunoId, int disciplinaId, Double nota) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean registrada = notaService.registrarNota(alunoId, disciplinaId, nota);
            if (registrada) {
                response.put("status", "success");
                response.put("message", "Nota registrada com sucesso");
            } else {
                response.put("status", "error");
                response.put("message", "Não foi possível registrar a nota");
            }
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> consultarNota(int alunoId, int disciplinaId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Double nota = notaService.consultarNota(alunoId, disciplinaId);
            response.put("status", "success");
            response.put("nota", nota);
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> calcularMediaAluno(int alunoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Double media = notaService.calcularMediaAluno(alunoId);
            response.put("status", "success");
            response.put("media", media);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
    
    public Map<String, Object> calcularMediaDisciplina(int disciplinaId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Double media = notaService.calcularMediaDisciplina(disciplinaId);
            response.put("status", "success");
            response.put("media", media);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return response;
    }
}
