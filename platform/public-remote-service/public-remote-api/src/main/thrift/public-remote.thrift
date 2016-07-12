namespace java com.rr.publik.api

struct IntegerListRequest {
	1:required list<string> result;
}
struct StringMapResponse {
	1:required map<string,string> result;
}

service GameService {
    
    i32 handle(1:string identity,2:string tel, 3:string message, 4:map<string,string> params);

    StringMapResponse sendMessages(1:IntegerListRequest req);

}
