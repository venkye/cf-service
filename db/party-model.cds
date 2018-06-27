namespace com.sap.service.PartyModel;

using com.sap.service.common as Common from './common-model';
using com.sap.service.TicketModel from './ticket-model';

entity Party{
	key UUID : UUID;
	BupaID : Common.PartyID;
	BupaUUID : UUID;
	TypeCode : String(15);
	RoleCode : String(10);
	MainIndicator : Boolean;
	
	Ticket : Association to TicketModel.Ticket;
}