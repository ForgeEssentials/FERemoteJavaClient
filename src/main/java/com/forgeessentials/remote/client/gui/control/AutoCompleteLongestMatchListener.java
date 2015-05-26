package com.forgeessentials.remote.client.gui.control;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AutoCompleteLongestMatchListener<T> {

    private final ComboBox<T> comboBox;

    private ObservableList<T> data;

    private volatile boolean autocompleting;

    public static <T> String completeMatching(String text, List<T> items)
    {
        List<String> matching = new ArrayList<>();
        int shortestMatch = Integer.MAX_VALUE;
        for (T item : items)
        {
            String itemText = item.toString();
            if (itemText.startsWith(text))
            {
                matching.add(itemText);
                shortestMatch = Math.min(itemText.length(), shortestMatch);
            }
        }
        if (matching.isEmpty())
            return text;
        int matchLength = text.length();
        matchLoop: for (; matchLength < shortestMatch; matchLength++)
        {
            char c = matching.get(0).charAt(matchLength);
            for (String item : matching)
                if (c != item.charAt(matchLength))
                    break matchLoop;
        }
        return matching.get(0).substring(0, matchLength);
    }

    public AutoCompleteLongestMatchListener(final ComboBox<T> comboBox, final ObservableList<T> data)
    {
        this.comboBox = comboBox;
        this.data = data;

        comboBox.setEditable(true);

        comboBox.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event)
            {
                if (event.getCode() == KeyCode.TAB)
                {
                    comboBox.getEditor().setText(completeMatching(comboBox.getEditor().getText(), data));
                    comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
                    event.consume();
                }
            }
        });

        comboBox.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event)
            {
                switch (event.getCode())
                {
                case UP:
                    moveCaretToEnd();
                    return;
                case DOWN:
                    if (!comboBox.isShowing())
                        comboBox.show();
                    moveCaretToEnd();
                    return;
                case RIGHT:
                case LEFT:
                case HOME:
                case END:
                    comboBox.hide();
                    return;
                default:
                    if (!comboBox.getItems().isEmpty())
                        comboBox.show();
                    break;
                }
            }
        });

        comboBox.getEditor().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (!autocompleting)
                    autocomplete();
            }
        });
    }

    public AutoCompleteLongestMatchListener(ComboBox<T> comboBox)
    {
        this(comboBox, comboBox.getItems());
    }

    public ObservableList<T> getData()
    {
        return data;
    }

    public void setData(ObservableList<T> data)
    {
        this.data = data;
    }

    private void moveCaretToEnd()
    {
        comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
    }

    private void autocomplete()
    {
        autocompleting = true;
        try
        {
            int cPos = comboBox.getEditor().getCaretPosition();
            String text = comboBox.getEditor().getText();
            String compareText = text; // text.toLowerCase();
            ObservableList<T> list = FXCollections.observableArrayList();
            for (T item : data)
                if (item.toString().toLowerCase().startsWith(compareText))
                    list.add(item);
            comboBox.setItems(list);
            comboBox.getEditor().setText(text);
            comboBox.getEditor().positionCaret(cPos);
        }
        finally
        {
            autocompleting = false;
        }
    }

}