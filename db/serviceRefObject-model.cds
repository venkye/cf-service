namespace com.sap.service.ServiceRefObjectModel;

using com.sap.service.common as Common from './common-model';
using com.sap.service.TicketModel from './ticket-model'; 

entity ServiceRefObject{
	key UUID : UUID; 
	ProductID : String(40);
	ProductDesc : String;
	ProductType : String;
	MainIndicator : Boolean;
	
	Ticket : Association to TicketModel.Ticket; 
}