package com.alterjoc.radar.common.data;

import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.common.serialization.JSONUtils;
import org.jboss.capedwarf.common.sql.Column;
import org.jboss.capedwarf.common.sql.OnDelete;
import org.jboss.capedwarf.common.sql.OnLoad;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Dejan Pazin
 * @author Ales Justin
 */
@OnLoad("com.alterjoc.radar.client.database.sqlite.OnLoadEventListener")
@OnDelete("com.alterjoc.radar.client.database.sqlite.OnDeleteEventListener")
public class EventInfo extends PhotoInfo {
    private static final long serialVersionUID = 1l;

    private String title;
    private String comment;
    private Long topicId;
    private String kind;
    private int latitude;
    private int longitude;
    private String address;
    private List<CommentInfo> userComments;
    private String username;
    private Type type;

    // For client side only:
    private TopicInfo topicInfo;
    private boolean userNotified;
    // Set to false if the user has not seen the details yet
    // Reset this attribute to false when new comments arrive for the event
    private boolean detailsRead;
    private boolean archived;
    // If the user unarchives an event, then the system should no longer auto archive it.
    private boolean unarchived;
    private transient Integer distance;
    private transient byte[] smallPhoto;
    // blob handling
    private Long smallPhotoPk;
    private Long bigPhotoPk;

    // For server side only

    private Long clientId;

    public EventInfo() {
    }

    public EventInfo(String title, String comment, Long topicId) {
        this.title = title;
        this.comment = comment;
        this.topicId = topicId;
    }

    public void writeJSONObject(JSONObject json) throws JSONException {
        super.writeJSONObject(json);

        json.put("topicId", topicId);
        json.putOpt("title", title);
        json.putOpt("comment", comment);

        json.put("latitude", latitude);
        json.put("longitude", longitude);
        json.putOpt("address", address);
        json.putOpt("username", username);
        JSONUtils.writeEnum(json, "type", type);

        JSONUtils.writeArray(json, "smallPhoto", smallPhoto);

        // impl detail -- (x < 0) marks no small photo
        if (smallPhotoPk != null && smallPhotoPk < 0)
            json.put("smallPhotoPk", smallPhotoPk);
    }

    public void readJSONObject(JSONObject json) throws JSONException {
        super.readJSONObject(json);

        topicId = json.getLong("topicId");
        title = json.optString("title");
        comment = json.optString("comment");

        latitude = json.getInt("latitude");
        longitude = json.getInt("longitude");
        address = json.optString("address");
        username = json.optString("username");
        type = JSONUtils.readEnum(json, "type", null, Type.class);

        smallPhoto = JSONUtils.readArray(json, "smallPhoto");

        // impl detail -- (x < 0) marks no small photo
        long temp = json.optLong("smallPhotoPk", 0);
        if (temp < 0)
            smallPhotoPk = temp;
    }

    @Size(max = 500000)
    public byte[] getSmallPhoto() {
        return smallPhoto;
    }

    public void setSmallPhoto(byte[] smallPhoto) {
        this.smallPhoto = smallPhoto;
    }

    @Size(max = 200)
    @Column
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column
    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Column(defaultValue = "0")
    public boolean isUnarchived() {
        return unarchived;
    }                     

    public void setUnarchived(boolean unarchived) {
        this.unarchived = unarchived;
    }

    @Column
    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    @Column
    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    @Size(max = 300)
    @Column
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Column
    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public TopicInfo getTopicInfo() {
        return topicInfo;
    }

    public void setTopicInfo(TopicInfo topicInfo) {
        this.topicInfo = topicInfo;
        this.topicId = topicInfo != null ? topicInfo.getId() : null;
    }

    @Column
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Column
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    @Column(defaultValue = "0")
    public boolean isUserNotified() {
        return userNotified;
    }

    public void setUserNotified(boolean userNotified) {
        this.userNotified = userNotified;
    }

    @Column(defaultValue = "0")
    public boolean isDetailsRead() {
        return detailsRead;
    }

    public void setDetailsRead(boolean detailsRead) {
        this.detailsRead = detailsRead;
    }

    @Column
    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    @Column
    public Long getSmallPhotoPk() {
        return smallPhotoPk;
    }

    public void setSmallPhotoPk(Long smallPhotoPk) {
        this.smallPhotoPk = smallPhotoPk;
    }

    @Column
    public Long getBigPhotoPk() {
        return bigPhotoPk;
    }

    public void setBigPhotoPk(Long bigPhotoPk) {
        this.bigPhotoPk = bigPhotoPk;
    }

    public List<CommentInfo> getUserComments() {
        return userComments;
    }

    public void setUserComments(List<CommentInfo> userComments) {
        this.userComments = userComments;
    }

    public String toShortString() {
        return "EventInfo [id: " + getId() + ", title: " + title + "]";
    }

   public boolean autoArchiveIfExpired()
   {
      if (isExpired() && isArchived() == false && isUnarchived() == false)
      {
         setArchived(true);
         return true;
      }
      return false;
   }
}
