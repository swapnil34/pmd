/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.value.Var;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.fxdesigner.model.LogEntry;
import net.sourceforge.pmd.util.fxdesigner.model.LogEntry.Category;
import net.sourceforge.pmd.util.fxdesigner.util.DesignerUtil;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;


/**
 * @author Clément Fournier
 * @since 6.0.0
 */
public class EventLogController implements Initializable {

    /**
     * Exceptions from XPath evaluation or parsing are never emitted
     * within less than that time interval to keep them from flooding the tableview.
     */
    private static final Duration PARSE_EXCEPTION_DELAY = Duration.ofMillis(3000);

    private final DesignerRoot designerRoot;
    private final MainDesignerController mediator;

    @FXML
    private TableView<LogEntry> eventLogTableView;
    @FXML
    private TableColumn<LogEntry, Date> logDateColumn;
    @FXML
    private TableColumn<LogEntry, Category> logCategoryColumn;
    @FXML
    private TableColumn<LogEntry, String> logMessageColumn;
    @FXML
    private TextArea logDetailsTextArea;

    private Var<List<Node>> selectedErrorNodes = Var.newSimpleVar(Collections.emptyList());


    public EventLogController(DesignerRoot owner, MainDesignerController mediator) {
        this.designerRoot = owner;
        this.mediator = mediator;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        logMessageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        logDateColumn.setCellValueFactory(entry -> new SimpleObjectProperty<>(entry.getValue().getTimestamp()));
        logDateColumn.setCellFactory(column -> new TableCell<LogEntry, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });

        EventStream<LogEntry> onlyParseException = designerRoot.getLogger().getLog()
                                                               .filter(x -> x.getCategory() == Category.PARSE_EXCEPTION)
                                                               .successionEnds(PARSE_EXCEPTION_DELAY);

        EventStream<LogEntry> onlyXPathException = designerRoot.getLogger().getLog()
                                                               .filter(x -> x.getCategory() == Category.XPATH_EVALUATION_EXCEPTION)
                                                               .successionEnds(PARSE_EXCEPTION_DELAY);

        EventStream<LogEntry> otherExceptions = designerRoot.getLogger().getLog()
                                                            .filter(x -> x.getCategory() != Category.PARSE_EXCEPTION)
                                                            .filter(y -> y.getCategory() != Category.XPATH_EVALUATION_EXCEPTION);

        EventStreams.merge(onlyParseException, otherExceptions, onlyXPathException)
                    .subscribe(t -> eventLogTableView.getItems().add(t));

        eventLogTableView.getSelectionModel()
                         .selectedItemProperty()
                         .addListener((obs, oldVal, newVal) -> onExceptionSelectionChanges(oldVal, newVal));

        EventStreams.combine(EventStreams.changesOf(eventLogTableView.focusedProperty()),
                             EventStreams.changesOf(selectedErrorNodes));

        EventStreams.valuesOf(eventLogTableView.focusedProperty())
                    .successionEnds(Duration.ofMillis(100))
                    .subscribe(b -> {
                        if (b) {
                            mediator.handleSelectedNodeInError(selectedErrorNodes.getValue());
                        } else {
                            mediator.resetSelectedErrorNodes();
                        }
                    });

        selectedErrorNodes.values().subscribe(mediator::handleSelectedNodeInError);

        eventLogTableView.resizeColumn(logMessageColumn, -1);

        logMessageColumn.prefWidthProperty()
                        .bind(eventLogTableView.widthProperty()
                                               .subtract(logCategoryColumn.getPrefWidth())
                                               .subtract(logDateColumn.getPrefWidth())
                                               .subtract(2)); // makes it work
        logDateColumn.setSortType(SortType.DESCENDING);

    }


    private void handleSelectedEntry(LogEntry entry) {
        if (entry == null) {
            selectedErrorNodes.setValue(Collections.emptyList());
            return;
        }
        switch (entry.getCategory()) {
        case OTHER:
            break;
        case PARSE_EXCEPTION:
            // TODO
            break;
        case TYPERESOLUTION_EXCEPTION:
        case SYMBOL_FACADE_EXCEPTION:
            DesignerUtil.stackTraceToXPath(entry.getThrown()).map(mediator::runXPathQuery).ifPresent(selectedErrorNodes::setValue);
            break;
        default:
            break;
        }
    }


    private void onExceptionSelectionChanges(LogEntry oldVal, LogEntry newVal) {
        logDetailsTextArea.setText(newVal == null ? "" : newVal.getStackTrace());

        if (!Objects.equals(newVal, oldVal)) {
            handleSelectedEntry(newVal);
        }
    }
}
