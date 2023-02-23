package com.icplaza.mapping.dao;

import com.icplaza.mapping.model.ConfModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfDAO {
    String table = "conf";

    @Insert("insert into " + table + " (`channel_type`,`fee`,`max_limit`) values(#{channelType},#{fee},#{maxLimit})")
    int add(ConfModel model);

    @Update("update " + table + " set fee=#{fee},max_limit=#{maxLimit} where channel_type=#{channelType}")
    boolean update(ConfModel model);

    @Select("select * from " + table + " where channel_type=#{channelType}")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "max_limit", property = "maxLimit"),
    })
    ConfModel selectModel(Integer channelType);

    @Select("select * from " + table)
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "max_limit", property = "maxLimit"),
    })
    List<ConfModel> selectModels();

}
