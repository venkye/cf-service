namespace com.sap.service.TicketModel;

using com.sap.service.common as Common from './common-model'; 
using com.sap.service.ItemModel from './item-model';
using com.sap.service.PartyModel from './party-model';
using com.sap.service.ServiceRefObjectModel from './serviceRefObject-model';
 
entity Ticket{
	key UUID : UUID;
	ID : String(35);
	PriorityCode : Common.PriorityCode;
	Subject : String(255);
	TypeCode : String(4);
	StatusCode : String(2);
	
	Items : Association to many ItemModel.Item on Items.Ticket = $self;
	Parties : Association to many PartyModel.Party on Parties.Ticket = $self;
	ServiceRefObjects : Association to many ServiceRefObjectModel.ServiceRefObject on ServiceRefObjects.Ticket = $self;
} 
