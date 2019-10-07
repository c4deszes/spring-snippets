package com.example.demo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.ParameterDescription;
import org.springframework.shell.ParameterResolver;
import org.springframework.shell.ValueResult;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Demonstrating the possibility of infinite arity 
 * 
 * Limitations:
 *  String only, although it'd be possible to use a conversion service
 *  Only for a single argument
 *  Having to annotate @ShellOption(optOut=true)
 *  
 */
@ShellComponent
public class ShellCommands {

    /**
     * Executes a process based on the input after 'syscmd'
     * 
     * Example usage on Windows: 'syscmd cmd.exe /c echo "Hello, World!"'
     */
    @ShellMethod(key = "syscmd", value = "Calls a system command")
    public void systemCommand(
        //Opting out of StandardParameterResolver so it falls back to our ParameterResolver
        @ShellOption(optOut = true) List<String> command) 
        
        throws IOException, InterruptedException {

        //Executing command and reading it's output
        String cmd = String.join(" ", command);
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = input.readLine();
        while(line != null) {
            System.out.println(line);
            line = input.readLine();
        }
    }

    /**
     * Custom parameter resolver to be used for Lists
     */
    @Bean
    public ParameterResolver commandParameterResolver() {
        return new ParameterResolver(){
        
            @Override
            public boolean supports(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(List.class);
            }
        
            /**
             * This implementation simply returns all the words (arguments) present
             * 'Infinite arity'
             */
            @Override
            public ValueResult resolve(MethodParameter methodParameter, List<String> words) {
                return new ValueResult(methodParameter, words);
            }
        
            @Override
            public Stream<ParameterDescription> describe(MethodParameter parameter) {
                return Stream.of(ParameterDescription.outOf(parameter));
            }
        
            @Override
            public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext context) {
                return Collections.emptyList();
            }
        };
    }
    
}