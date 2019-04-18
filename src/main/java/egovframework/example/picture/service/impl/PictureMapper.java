package egovframework.example.picture.service.impl;

import java.util.List;
import java.util.Map;

import egovframework.example.picture.service.PagingVO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.rte.psl.dataaccess.util.EgovMap;

@Mapper("pictureMapper")
public interface PictureMapper {

	void insertPitureBoardInfo(EgovMap insertInfo) throws Exception;

	EgovMap selectPictureDetail(String parameter) throws Exception;

	void insertPictureBoardReply(EgovMap param) throws Exception;

	List<EgovMap> selectPictureBoardReplyList(String parameter) throws Exception;

	void updateReply(Map<String, Object> resMap) throws Exception;

	void deleteReply(Map<String, Object> resMap) throws Exception;

	void deleteFavorite(Map<String, Object> resMap) throws Exception;

	void insertFavorite(Map<String, Object> resMap) throws Exception;

	String myFavorite(EgovMap parameter) throws Exception;

	List<EgovMap> selectPictureList(PagingVO pageParam)throws Exception;

	List<EgovMap> selectNoticeList()throws Exception;

	int selectPictureCount()throws Exception;

	List<EgovMap> selectFavoritePictureList(PagingVO pageParam) throws Exception;

}
