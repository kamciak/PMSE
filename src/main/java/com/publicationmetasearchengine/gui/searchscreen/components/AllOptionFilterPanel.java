/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.gui.searchscreen.components;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.data.filters.FilterType;
import com.publicationmetasearchengine.gui.Icon;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author Kamciak
 */
public class AllOptionFilterPanel extends PMSEPanel implements Filter {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AllOptionFilterPanel.class);

    private static enum Logic {
        ALL_AND("Inner AND, outer AND", ComboCondition.Op.AND, ComboCondition.Op.AND),
        INNER_OR("Inner OR, outer AND", ComboCondition.Op.OR, ComboCondition.Op.AND),
        INNER_AND("Inner AND, outer OR", ComboCondition.Op.AND, ComboCondition.Op.OR),
        ALL_OR("Inner OR, outer OR", ComboCondition.Op.OR, ComboCondition.Op.OR);

        private final String cbCaption;
        private final ComboCondition.Op innerLogic;
        private final ComboCondition.Op outerLogic;

        public static class UndeterminatedLogicException extends Exception {
            private static final long serialVersionUID = 1L;

            public UndeterminatedLogicException(String message) {
                super(message);
            }
        }

        private Logic(String cbCaption, ComboCondition.Op innerLogic, ComboCondition.Op outerLogic) {
            this.cbCaption = cbCaption;
            this.innerLogic = innerLogic;
            this.outerLogic = outerLogic;
        }

        public String getCbCaption() {
            return cbCaption;
        }

        public ComboCondition.Op getInnerLogic() {
            return innerLogic;
        }

        public ComboCondition.Op getOuterLogic() {
            return outerLogic;
        }

        public static AllOptionFilterPanel.Logic determinateLogic(ComboCondition.Op innerLogic, ComboCondition.Op outerLogic) throws AllOptionFilterPanel.Logic.UndeterminatedLogicException {
            if (innerLogic == ComboCondition.Op.AND && outerLogic == ComboCondition.Op.OR)
                return INNER_AND;
            if (innerLogic == ComboCondition.Op.OR && outerLogic == ComboCondition.Op.AND)
                return INNER_OR;
            if (innerLogic == ComboCondition.Op.AND && outerLogic == ComboCondition.Op.AND)
                return ALL_AND;
            if (innerLogic == ComboCondition.Op.OR && outerLogic == ComboCondition.Op.OR)
                return ALL_OR;
            throw new AllOptionFilterPanel.Logic.UndeterminatedLogicException(innerLogic + " - " + outerLogic);
        }
    }

    private final FilterType filterType;
    private final ComboBox filterLogicCB = new ComboBox("Logic");
    private final TextField mainTextField = new TextField();
    private final AllOptionFilterPanel.AddBtn addBtn;
    private final ArrayList<TextField> additionalTextFields = new ArrayList<TextField>();

    private final VerticalLayout mainLayout = new VerticalLayout();
    private final HorizontalLayout mainTextFieldHl = new  HorizontalLayout();
    private final PMSEButton clearBtn = new PMSEButton("clear");


    private class AddBtn extends PMSEButton {
        private static final long serialVersionUID = 1L;
        private HorizontalLayout currentLayout;

        public AddBtn(HorizontalLayout currentLayout) {
            super("");
            this.currentLayout = currentLayout;
            setStyleName("link");
            setIcon(Icon.FILTER_ADD.getResource());
        }

        public void setCurrentLayout(HorizontalLayout currentLayout) {
            this.currentLayout = currentLayout;
            currentLayout.addComponent(this);
        }

        public HorizontalLayout getCurrentLayout() {
            return currentLayout;
        }
    }

    public AllOptionFilterPanel(String caption, FilterType filterType) {
        super(caption);
        this.filterType = filterType;
        setSizeUndefined();
        mainLayout.setSizeUndefined();
        mainLayout.setWidth("100%");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        filterLogicCB.setNullSelectionAllowed(false);
        for (AllOptionFilterPanel.Logic logic : AllOptionFilterPanel.Logic.values()) {
            filterLogicCB.addItem(logic);
            filterLogicCB.setItemCaption(logic, logic.getCbCaption());
        }
        filterLogicCB.select(AllOptionFilterPanel.Logic.ALL_AND);

        clearBtn.setStyleName("link");
        clearBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                clear();
            }
        });
        HorizontalLayout lowerLayout = new HorizontalLayout();
        lowerLayout.addComponent(filterLogicCB);
        lowerLayout.addComponent(clearBtn);
        lowerLayout.setWidth("100%");

        addBtn = new AllOptionFilterPanel.AddBtn(mainTextFieldHl);
        addBtn.setImmediate(true);
        mainTextFieldHl.addComponent(mainTextField);
        mainTextFieldHl.addComponent(addBtn);
        mainLayout.addComponent(mainTextFieldHl);
        mainLayout.addComponent(lowerLayout);
        addBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                final TextField textField = new TextField();
                additionalTextFields.add(textField);
                final HorizontalLayout hl = new HorizontalLayout();
                PMSEButton delBtn = new PMSEButton();
                delBtn.setIcon(Icon.FILTER_DEL.getResource());
                delBtn.setStyleName("link");
                delBtn.addListener(new Button.ClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        mainLayout.removeComponent(hl);
                        additionalTextFields.remove(textField);
                        addBtn.getCurrentLayout().removeComponent(addBtn);
                        if (mainLayout.getComponentCount() <= 2)
                            mainTextFieldHl.addComponent(addBtn);
                        else {
                           HorizontalLayout hl = (HorizontalLayout) mainLayout.getComponent(mainLayout.getComponentCount()-2);
                           hl.addComponent(addBtn);
                        }
                    }
                });
                hl.addComponent(textField);
                hl.addComponent(delBtn);

                mainLayout.addComponent(hl, mainLayout.getComponentCount()-1);
                addBtn.getCurrentLayout().removeComponent(addBtn);
                hl.addComponent(addBtn);
            }
        });

        setContent(mainLayout);
    }

    @Override
    public FilterCriteria getFilterCriteria(){
        ArrayList<String> values = new ArrayList<String>();
        if (mainTextField.getValue()!= null && !((String)mainTextField.getValue()).isEmpty())
            values.add((String)mainTextField.getValue());
        for (TextField textField : additionalTextFields)
            if (textField.getValue()!= null && !((String)mainTextField.getValue()).isEmpty())
                values.add((String)textField.getValue());

        AllOptionFilterPanel.Logic logic = (AllOptionFilterPanel.Logic) filterLogicCB.getValue();
        return new FilterCriteria(filterType, values, logic.getInnerLogic(), logic.getOuterLogic());
    }

    @Override
    public void setFilterCriteria(FilterCriteria filterCriteria) {
        final ArrayList<String> values = filterCriteria.getValues();
        try {
            AllOptionFilterPanel.Logic logic = AllOptionFilterPanel.Logic.determinateLogic(filterCriteria.getInnerOperator(), filterCriteria.getOuterOperator());
            filterLogicCB.select(logic);
        } catch (AllOptionFilterPanel.Logic.UndeterminatedLogicException ex) {
            LOGGER.fatal("Should not occure!!!", ex);
            Notificator.showNotification(getApplication(), "Error", "Internal error.\nTry again later.\nIf it repeates, please notify administrator.", Notificator.NotificationType.ERROR);
        }
        if (values != null && values.size() > 0) {
            mainTextField.setValue(values.get(0));
            for (int i = 1; i < values.size(); ++i) {
                addBtn.click();
                additionalTextFields.get(i-1).setValue(values.get(i));
            }
        }
    }

    @Override
    public void clear() {
        mainTextField.setValue("");
        additionalTextFields.clear();
        for (int i = mainLayout.getComponentCount()-2; i >= 1; --i)
            mainLayout.removeComponent(mainLayout.getComponent(i));
        mainTextFieldHl.addComponent(addBtn);
        filterLogicCB.setValue(AllOptionFilterPanel.Logic.ALL_AND);
    }
}
