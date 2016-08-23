package Types;

import org.json.simple.JSONObject;

import Util.JSONUtil;

public class VineTag {

	public long tagId;
	public String tag;
	
	public VineTag(JSONObject data){
		if(data!=null){
			tagId=JSONUtil.getLong(data, "tagId");
			tag=JSONUtil.getString(data, "tag");
		}
	}
	
}
