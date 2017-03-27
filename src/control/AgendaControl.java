package control; 

import java.util.Comparator;
import java.util.Iterator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;
import model.Event;
import model.EventDetails;
import model.Status;
import model.TaskDetails;
import model.storage.EventCollection;
import javafx.scene.layout.AnchorPane;

public class AgendaControl {
	@FXML private ListView <Event> agendaList;
    @FXML private Button removeButton;
    @FXML private Button doneButton;
	@FXML private AnchorPane pane;
    
    @FXML 
    public void initialize(){ 
    	assert agendaList != null :"fx:id=\"agendaList\" was not injected: check your FXML file 'AgendaPane.fxml'.";
    	assert doneButton != null :"fx:id=\"doneButton\" was not injected: check your FXML file 'AgendaPane.fxml'.";
    	assert removeButton != null :"fx:id=\"removeButton\" was not injected: check your FXML file 'AgendaPane.fxml'.";
    	
    	agendaList.setCellFactory(new Callback<ListView<Event>, ListCell<Event>>() {
			
			@Override
			public ListCell<Event> call(ListView<Event> arg0) {
				return new ListCell<Event>() {
					 @Override 
					 protected void updateItem(Event item, boolean empty) {
						 super.updateItem(item, empty);
					    	if (item == null || empty) {
					            setText(null);
					            setGraphic(null);
					        } else {
					        	String time = "";
					        	if(item.getDetails() instanceof EventDetails )
					    			time += item.getDetails().getTimeStart() +"  -  " +((EventDetails)item.getDetails()).getTimeEnd() + " "; 
					    	    if(item.getDetails() instanceof TaskDetails)
					    	    	time += item.getDetails().getTimeStart() +"	         ";
					    	    
					        	Text txtTime = new Text(time);
					            Text txtTitle = new Text(item.getTitle());
					            Color color = item.getDetails().getColor();
					            
					            txtTime.setFill(color);
					            txtTitle.setFill(color);
					            
					            if (item.getDetails() instanceof TaskDetails)
					            	if(((TaskDetails) item.getDetails()).getStatus() == Status.DONE)
					            		txtTitle.setStyle("-fx-strikethrough: true");
					            	
					            txtTime.setFont(Font.font(null, FontWeight.BOLD, 16));
					            txtTitle.setFont(Font.font(null, FontWeight.NORMAL, 16));
					            
					            HBox hbox = new HBox(txtTime, txtTitle);
					            setGraphic(hbox);
					        }
					 }
				};
			}
		});
    }
    
    public void setEvents (Iterator <Event> events) {
		ObservableList<Event> items = FXCollections.observableArrayList();
		
		while(events.hasNext()){ 
			Event e = events.next();
				items.add(e);
		}
		
		items.sort(new Comparator <Event> () {
			@Override
			public int compare(Event o1, Event o2) {
				return o1.getDetails().getTimeStart().compareTo(o2.getDetails().getTimeStart());
			}
		});
		
		items = FXCollections.observableArrayList(items);
		agendaList.setItems(FXCollections.observableArrayList());
		agendaList.setItems(items);
    }
    
    public void initializeButtons (EventCollection collections) {
    	setDoneButton(collections);
    	setRemoveButton(collections);
    }
    
    private void setDoneButton(EventCollection collections){
    	doneButton.setOnAction( event -> {
    		if(!agendaList.getSelectionModel().isEmpty()){
				Event e = agendaList.getSelectionModel().getSelectedItem();
		     
		    	collections.openDB();
		    	
		    	if(e.getDetails() instanceof TaskDetails) {
		    		((TaskDetails)e.getDetails()).setStatus(Status.DONE);
			  		
			    	boolean updated = collections.update(e);
			    	
		    		if(updated) {
			   		    Alert alert = new Alert(AlertType.INFORMATION);
		       			alert.setTitle("Success");
		       			alert.setHeaderText(null);
		       			alert.setContentText("Marked as DONE!");
		       			alert.showAndWait();
					} else {
						Alert alert = new Alert(AlertType.ERROR);
		       			alert.setTitle("Database Error");
		       			alert.setHeaderText(null);
		       			alert.setContentText("Database failed to update!");
		       			alert.showAndWait();
					}			
				} else {
					Alert alert = new Alert(AlertType.ERROR);
		   			alert.setTitle("Not a task");
		   			alert.setHeaderText(null);
		   			alert.setContentText("Selected item is an event not a task!");
		   			alert.showAndWait();
				}
			    collections.closeDB();
			} else { 
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("No Tasks Selected");
					alert.setHeaderText(null);
					alert.setContentText("No tasks were selected!");
		    		alert.showAndWait();
	    	}
	    });
	}

    private void setRemoveButton(EventCollection collections) {
		removeButton.setOnAction(event -> {
			if (agendaList.getSelectionModel().isEmpty()){
    	    	Alert alert = new Alert(AlertType.ERROR);
    			alert.setTitle("Error Dialog");
    			alert.setHeaderText("Look, an Error Dialog");
    			alert.setContentText("Ooops, no Agendas were selected!");
    			alert.showAndWait();
    	    }
			if(!agendaList.getSelectionModel().isEmpty() && agendaList.getSelectionModel().getSelectedItem().getDetails() instanceof TaskDetails){ 
				Event e = agendaList.getSelectionModel().getSelectedItem();
		    	collections.openDB();
		    	
				if(collections.remove(e)){
					Alert alert = new Alert(AlertType.ERROR);
        			alert.setTitle("Notification");
        			alert.setHeaderText("Look, a Notification");
        			alert.setContentText("Task you have chosen has been deleted!");
        			alert.showAndWait();
				}else{
					Alert alert = new Alert(AlertType.ERROR);
        			alert.setTitle("Error Dialog");
        			alert.setHeaderText("Look, an Error Dialog");
        			alert.setContentText("Ooops, Task failed to be deleted!");
        			alert.showAndWait();
				}			
				collections.closeDB();
			} else if(agendaList.getSelectionModel().getSelectedItem().getDetails() instanceof EventDetails){
				Alert alert = new Alert(AlertType.ERROR);
    			alert.setTitle("Error Dialog");
    			alert.setHeaderText("Look, an Error Dialog");
    			alert.setContentText("Ooops, you can't delete an event!");
    			alert.showAndWait();
    	    } 
		});
    }
}
