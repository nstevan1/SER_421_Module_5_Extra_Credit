package com.ser421.eliza.service;

import com.ser421.eliza.model.ElizaDictionary;
import com.ser421.eliza.model.Entry;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.*;

@Service
public class ElizaService {

    private final ElizaDictionary dictionary;
    private final Random random = new Random();

    public ElizaService(ObjectMapper objectMapper) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("default.json");
            dictionary = objectMapper.readValue(is, ElizaDictionary.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dictionary", e);
        }
    }

    public String getInitialGreeting(String name, HttpSession session) {
        String[] greetings = {
                name + ", how is your day going?",
                name + ", is something troubling you?",
                name + ", you seem happy, why is that?"
        };
        String greeting = greetings[random.nextInt(greetings.length)];
        saveConversation(session, "Eliza", greeting);
        return greeting;
    }

    public String getResponse(String userInput, String name, HttpSession session) {
        String lowerInput = userInput.toLowerCase();
        List<Entry> entries = dictionary.getEntries();

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.getKey().stream().anyMatch(k -> lowerInput.contains(k.toLowerCase()))) {
                String response = getCycledResponse(entry, i, session);
                saveConversation(session, "Eliza", response);
                return response;
            }
        }

        String[] defaults = {
                "Tell me more about that. How does that make you feel?",
                "Interesting. Can you elaborate?",
                "I see. What else is on your mind?"
        };
        String resp = defaults[random.nextInt(defaults.length)];
        saveConversation(session, "Eliza", resp);
        return resp;
    }

    private String getCycledResponse(Entry entry, int entryIndex, HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Set<Integer>> used = (Map<Integer, Set<Integer>>) session.getAttribute("usedResponses");
        if (used == null) {
            used = new HashMap<>();
            session.setAttribute("usedResponses", used);
        }

        Set<Integer> ansUsed = used.computeIfAbsent(entryIndex * 2, k -> new HashSet<>());
        Set<Integer> qUsed = used.computeIfAbsent(entryIndex * 2 + 1, k -> new HashSet<>());

        int ansIdx = getRandomUnused(entry.getAnswer().size(), ansUsed);
        int qIdx = getRandomUnused(entry.getQuestion().size(), qUsed);

        String answer = entry.getAnswer().get(ansIdx);
        String question = entry.getQuestion().get(qIdx);

        return answer + " " + question;
    }

    private int getRandomUnused(int max, Set<Integer> used) {
        if (used.size() >= max) used.clear();
        int idx;
        do {
            idx = random.nextInt(max);
        } while (used.contains(idx));
        used.add(idx);
        return idx;
    }

    private void saveConversation(HttpSession session, String sender, String text) {
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) session.getAttribute("conversation");
        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute("conversation", history);
        }
        history.add(sender + ": " + text);
    }

    public List<String> getConversation(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) session.getAttribute("conversation");
        return history != null ? history : new ArrayList<>();
    }

    public void clearSession(HttpSession session) {
        session.invalidate();
    }
}