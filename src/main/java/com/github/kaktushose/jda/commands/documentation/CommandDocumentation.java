package com.github.kaktushose.jda.commands.documentation;

import com.github.kaktushose.jda.commands.data.CommandList;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class automatically generates documentation for commands in Markdown format.
 *
 * @author Kaktushose
 * @version 1.1.0
 * @since 1.1.0
 */
public class CommandDocumentation {

    private static final Logger log = LoggerFactory.getLogger(CommandDocumentation.class);
    private final CommandList commandList;
    private final StringBuilder docs;
    private final String prefixPattern;
    private final String prefix;

    /**
     * Creates a new CommandDocumentation.
     *
     * @param commandList   the {@link CommandList} to create the documentation for
     * @param prefixPattern the prefix placeholder that is used in
     *                      {@link com.github.kaktushose.jda.commands.annotations.Command Command} annotations.
     *                      The default value is {@code {prefix}}
     * @param prefix        the prefix to replace the pattern with
     */
    public CommandDocumentation(@NotNull CommandList commandList, @NotNull String prefixPattern, @NotNull String prefix) {
        this.commandList = commandList;
        this.prefixPattern = prefixPattern;
        this.prefix = prefix;
        docs = new StringBuilder();
    }

    /**
     * Generates the markdown.
     */
    public void generate() {
        docs.append(String.format("> Auto generated command manual | %s",
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()))
        ).append("\n\n");

        commandList.getSortedByCategories().forEach(((category, commandCallables) -> {
            docs.append(new Heading(replace(category), 1)).append("\n");
            commandCallables.forEach(command -> {
                docs.append(new Heading(replace(command.getMetadata().getName()), 3)).append("\n\n");
                docs.append(new BoldText("Description:")).append("\n\n");
                docs.append(replace(command.getMetadata().getDescription())).append("\n\n");
                docs.append(new BoldText("Usage:")).append("\n\n");
                docs.append(String.format("`%s`", replace(command.getMetadata().getUsage()))).append("\n\n");
                List<String> labels = command.getLabels().stream().skip(1).collect(Collectors.toList());
                if (labels.size() > 0) {
                    docs.append(new BoldText(("Aliases:"))).append("\n\n");
                    docs.append(new UnorderedList<>(labels)).append("\n\n");
                }
                docs.append(new BoldText("Permissions:")).append("\n\n");
                docs.append(new UnorderedList<>(new ArrayList<>(command.getPermissions()))).append("\n\n");
            });
        }));
    }

    /**
     * Writes the markdown output into a file.
     *
     * @param file the file to save the markdown in
     */
    public void saveToFile(@NotNull File file) {
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.write(docs.toString());
        } catch (FileNotFoundException e) {
            log.error("Unable to write to file", e);
        }
    }

    /**
     * Gets the String containing the markdown.
     *
     * @return the markdown string
     */
    public String toString() {
        return docs.toString();
    }

    private String replace(String raw) {
        return raw.replaceAll(Pattern.quote(prefixPattern), prefix);
    }
}