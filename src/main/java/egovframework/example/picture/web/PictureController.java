package egovframework.example.picture.web;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import egovframework.example.cmmn.JsonUtil;
import egovframework.example.cmmn.service.CmmnVO;
import egovframework.example.picture.service.PagingVO;
import egovframework.example.picture.service.PictureService;
import egovframework.rte.psl.dataaccess.util.EgovMap;

@Controller
public class PictureController {
	
	@Resource(name = "pictureService")
	private PictureService pictureService;

	@RequestMapping("pictureUpload.do")
	public String pictureUpload(MultipartHttpServletRequest request) throws Exception{
		//MultipartResolver에 의해 Multipart요청을 분기해 받음 
		//MultipartHttpServletRequest는 MultipartRequest과 HttpServletRequest를 상속 받고있다.
		HttpSession session = request.getSession(false);
		//톰캣의 경로에 대한것
		//실 서버가 아닌 이클립스에서 톰캣을 돌리면 톰켓에서 웹어플을 돌리기위해 deploy 하게 되면서 
		//본래의 워크스페이스가 아닌 경로에 저장하게 됨 , 서블릿을 생성한 위치 자체가 바뀌기 때문에 원 의도와 다르게 패스가 잡힘
		//String path = request.getSession().getServletContext().getRealPath("/") + "upload\\";
		//그 위치에 업로드를 해도 임시파일과 같기 때문에 clean같은 작업을 진행하면 상태를 보장할 수 없다.
		//때문에 최상위 C:에 저장하기로 하였다.
		
		String path = "C:\\";
		File folder = new File(path, "myShoppingUpload");
		
		
		//폴더나 파일이 있는지 확인
		if (!folder.exists()) {
			try{

				folder.mkdir();
			} catch(Exception e){
				
				e.getStackTrace();
			}
		}
		
		/*확장자 구하기
		String[] temp 		= file.getOriginalFilename().split("\\.");
		int suffixLocation	= temp.length-1;
		String suffix		= temp[suffixLocation];
		*/
		
		MultipartFile file	= request.getFile("file");
		UUID uniqueCode		= UUID.randomUUID();
		String filename 	= uniqueCode + " " + file.getOriginalFilename();
		
		try {
			
			File saveFile = new File(path, "myShoppingUpload\\"+filename);
			file.transferTo(saveFile);
			System.out.println(saveFile.getAbsolutePath());
		} catch(Exception e) {
			
			e.printStackTrace();
		}
		EgovMap insertInfo = new EgovMap();
		
		insertInfo.put("title",		request.getParameter("title"));
		insertInfo.put("content",	request.getParameter("content"));
		insertInfo.put("writer",	session.getAttribute("id"));
		insertInfo.put("url", 		"uploaded//" + filename);
		
		pictureService.insertPitureBoardInfo(insertInfo);
	
		return "redirect:/pictureMain.do";
	}
	
	@RequestMapping("setReply.do")
	public String setReply(HttpServletRequest request) throws Exception {
		
		EgovMap param = new EgovMap();
		
		System.out.println(request.getParameter("writer"));
		
		param.put("boardNo",	request.getParameter("boardNo"));
		param.put("writer",		request.getParameter("writer"));
		param.put("content",	request.getParameter("content"));
		
		
		System.out.println("replySet");
		
		pictureService.insertPictureBoardReply(param);
		
		
		return "redirect:pictureDetail.do?seq_no="+param.get("boardNo");
	}
	
	@RequestMapping("modReply.do")
	public void modReply(@RequestBody String reqParam, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		System.out.println(reqParam);
		
		Map<String, Object> resMap = JsonUtil.JsonToMap(reqParam);;
	
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		try{
			
			pictureService.updateReply(resMap);
		} catch(Exception e) {
			
			e.getStackTrace();
		}
		
		resultMap.put("result", "success");
		
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		out.write(JsonUtil.HashMapToJson(resultMap));
	}
	
	@RequestMapping("deleteReply.do")
	public void deleteReply(@RequestBody String reqParam, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		System.out.println(reqParam);
		
		Map<String, Object> resMap = JsonUtil.JsonToMap(reqParam);;
	
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		try{
			
			pictureService.deleteReply(resMap);
		} catch(Exception e) {
			
			e.getStackTrace();
		}
		
		resultMap.put("result", "success");
		
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		out.write(JsonUtil.HashMapToJson(resultMap));
	}
	
	
	@RequestMapping("pictureDetail.do")
	public String pictureDetail(HttpServletRequest request, ModelMap model) throws Exception{
		
		//리플 가져오기
		System.out.println("response");
		
		EgovMap pictureDetail = pictureService.selectPictureDetail(request.getParameter("seq_no"));
		List<EgovMap> picturBoradReplyList = pictureService.selectPictureBoardReplyList(request.getParameter("seq_no"));
		
		HttpSession session = request.getSession(false);
		EgovMap param = new EgovMap();
		param.put("id", session.getAttribute("id"));
		param.put("seq_no", request.getParameter("seq_no"));
		
		String myFavorite = null;
		
		if(session != null) {
			myFavorite = pictureService.myFavorite(param);
			pictureDetail.put("myFavorite", myFavorite);
		}

		System.out.println(myFavorite);
		System.out.println(picturBoradReplyList);
		
		model.addAttribute("pictureDetail", 		pictureDetail);
		model.addAttribute("picturBoradReplyList",	picturBoradReplyList);
		
		return "picture/detail.pop";
	}
	
	@RequestMapping("pictureWrite.do")
	public String pictureWrite() throws Exception {
		
		return "picture/write.tiles";
	}

	
	@RequestMapping("pictureMain.do")
	public String pictureMain(HttpServletRequest request, ModelMap model) throws Exception{

		//database 쿼리 가져오는 부분
		PagingVO pageParam = new PagingVO();
		List<EgovMap> pictureBoardList = pictureService.selectPictureList(pageParam);
		int pictureListCount = pictureService.selectPictureCount();

		//paging 처리 부분
		int page		= (int) pageParam.getPage();
		int pageScale	= (int) pageParam.getPageScale();
		int pageGroup	= (page-1) / pageScale + 1;
		int startPage	= (pageGroup-1)*(pageScale) + 1;
		int endPage		= pageGroup*pageScale; 
		int lastPage	= pictureListCount/(int)pageParam.getRows() + 1;
		int lastGroup	= (lastPage-1) / pageScale + 1;
		
		if (endPage > lastPage) {
			
			endPage = lastPage;
		}

		model.addAttribute("page", page);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("pageGroup", pageGroup);
		model.addAttribute("pageScale", pageScale);
		model.addAttribute("lastPage", lastPage);
		model.addAttribute("lastGroup", lastGroup);
		
		model.addAttribute("pictureListCount", pictureListCount);
		model.addAttribute("pictureList", pictureBoardList);

		model.addAttribute("viewMode", "lastView");
		
		return "picture/main.tiles";
	}
	
	@RequestMapping("getPicturePage.do")
	public String getPage(HttpServletRequest request, ModelMap model) throws Exception {
		
		PagingVO pageParam = new PagingVO();
		pageParam.setPage(Long.parseLong(request.getParameter("page")));
		String viewMode = request.getParameter("viewMode");
		
		List<EgovMap> pictureList = null;
		switch(viewMode){
			
		case "lastView":
			pictureList = pictureService.selectPictureList(pageParam);
			break;
		case "favoriteView":
			pictureList = pictureService.selectFavoritePictureList(pageParam);
			break;
		default:
		
		}
		
		int pictureListCount = pictureService.selectPictureCount();

		int page		= (int) pageParam.getPage();
		int pageScale	= (int) pageParam.getPageScale();
		int pageGroup	= (page - 1) / pageScale + 1;
		int startPage	= (pageGroup-1)*(pageScale) + 1;
		int endPage		= pageGroup*pageScale; 
		int lastPage	= pictureListCount/(int)pageParam.getRows() + 1;
		int lastGroup	= lastPage / pageScale + 1;
		
		if (endPage > lastPage) {
			
			endPage = lastPage;
		}
		
		model.addAttribute("pictureListCount", pictureListCount);
		model.addAttribute("pictureList", pictureList);
		model.addAttribute("page", page);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("pageGroup", pageGroup);
		model.addAttribute("pageScale", pageParam.getPageScale());
		model.addAttribute("lastPage", lastPage);
		model.addAttribute("lastGroup", lastGroup);
		model.addAttribute("viewMode", viewMode);
		
		return "picture/main.tiles";
	}
	
	@RequestMapping("favoritePictureList.do")
	public String sortByFavoritePictureList(HttpServletRequest request, ModelMap model) throws Exception {
		
		PagingVO pageParam = new PagingVO();
		
		List<EgovMap> pictureList = pictureService.selectFavoritePictureList(pageParam);
		
		int pictureListCount = pictureService.selectPictureCount();

		int page		= (int) pageParam.getPage();
		int pageScale	= (int) pageParam.getPageScale();
		int pageGroup	= (page - 1) / pageScale + 1;
		int startPage	= (pageGroup-1)*(pageScale) + 1;
		int endPage		= pageGroup*pageScale; 
		int lastPage	= pictureListCount/(int)pageParam.getRows() + 1;
		int lastGroup	= lastPage / pageScale + 1;
		
		if (endPage > lastPage) {
			
			endPage = lastPage;
		}
		
		System.out.println(pictureListCount);
		System.out.println(startPage);
		System.out.println(endPage);
		System.out.println(lastGroup);
		
		model.addAttribute("pictureListCount", pictureListCount);
		model.addAttribute("pictureList", pictureList);
		model.addAttribute("page", page);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("pageGroup", pageGroup);
		model.addAttribute("pageScale", pageParam.getPageScale());
		model.addAttribute("lastPage", lastPage);
		model.addAttribute("lastGroup", lastGroup);
		model.addAttribute("viewMode", "favoriteView");
		
		return "picture/main.tiles";
	}

	@RequestMapping("writerPictureBoardList.do")
	public String writerPictureBoardList(HttpServletRequest request, ModelMap model) throws Exception {
		
		PagingVO pageParam = new PagingVO();
		
		String writer = request.getParameter("writer");
		pageParam.setKeyword(writer);
		
		System.out.println(pageParam.getKeyword());
		List<EgovMap> pictureList = pictureService.selectPictureListByWriter(pageParam);
		int pictureListCount = pictureService.selectPictureListCountByWriter(writer);

		System.out.println(pictureListCount);
		int page		= (int) pageParam.getPage();
		int pageScale	= (int) pageParam.getPageScale();
		int pageGroup	= (page - 1) / pageScale + 1;
		int startPage	= (pageGroup-1)*(pageScale) + 1;
		int endPage		= pageGroup*pageScale; 
		int lastPage	= pictureListCount/(int)pageParam.getRows() + 1;
		int lastGroup	= lastPage / pageScale + 1;
		
		if (endPage > lastPage) {
			
			endPage = lastPage;
		}
	
		model.addAttribute("pictureListCount", pictureListCount);
		model.addAttribute("pictureList", pictureList);
		model.addAttribute("page", page);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("pageGroup", pageGroup);
		model.addAttribute("pageScale", pageParam.getPageScale());
		model.addAttribute("lastPage", lastPage);
		model.addAttribute("lastGroup", lastGroup);
		model.addAttribute("viewMode", "favoriteView");
		
		return "picture/main.tiles";
	}
	
	@RequestMapping("pictureBoardSearch.do")
	public String pictureBoardSearch(HttpServletRequest request, ModelMap model) throws Exception {

		//VO의 정보를 꺼내서 맵에 세팅하고 맵에 원하는 정보를 더 넣는 방법
		String keyword = request.getParameter("keyword");
		PagingVO pagingVO = new PagingVO();
		
		CmmnVO cmmn = new CmmnVO();
		
		Map<Object, Object> pageParam = new HashMap<Object, Object>();
		
		Field[] fields = cmmn.getClass().getDeclaredFields();
		
		for(Field field : fields){
			field.setAccessible(true); //VO의 필드는 private이기 때문에 필드 접근가능하게 만들어줌
			pageParam.put(field.getName(), field.get(pagingVO));
		}
		pageParam.put("keyword", keyword);
		pageParam.put("rows", 16);
		
		System.out.println(pageParam);
		
		List<EgovMap> pictureList = pictureService.selectSearchPictureList(pageParam);
		
		System.out.println(pictureList);
		
		int pictureListCount = pictureService.selectSearchPictureListCount(keyword);

		int page		= (int) pagingVO.getPage();
		int pageScale	= (int) pagingVO.getPageScale();
		int pageGroup	= (page - 1) / pageScale + 1;
		int startPage	= (pageGroup-1)*(pageScale) + 1;
		int endPage		= pageGroup*pageScale; 
		int lastPage	= pictureListCount/(int) pagingVO.getRows() + 1;
		int lastGroup	= lastPage / pageScale + 1;
		
		if (endPage > lastPage) {
			
			endPage = lastPage;
		}
		
		System.out.println(pictureListCount);
		System.out.println(startPage);
		System.out.println(endPage);
		System.out.println(lastGroup);
		
		model.addAttribute("pictureListCount", pictureListCount);
		model.addAttribute("pictureList", pictureList);
		model.addAttribute("page", page);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("pageGroup", pageGroup);
		model.addAttribute("pageScale", pagingVO.getPageScale());
		model.addAttribute("lastPage", lastPage);
		model.addAttribute("lastGroup", lastGroup);
		model.addAttribute("viewMode", "favoriteView");
		
		return "picture/main.tiles";
	}
	
	@RequestMapping("favorite.do")
	public void favorite (@RequestBody String reqParam, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		Map<String, Object> resMap = JsonUtil.JsonToMap(reqParam);;
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		
		System.out.println(resMap.get("favoriteState"));
		
		try{
			//좋아요 상태일때 -> 취소
			if(resMap.get("favoriteState").equals("favorite")){
				pictureService.deleteFavorite(resMap);
			} else { //좋아요 상태가 아닐때 -> 좋아요
				pictureService.insertFavorite(resMap);
			}
			
		} catch(Exception e) {
			
			e.getStackTrace();
		}
		
		resultMap.put("result", "success");
		
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		out.write(JsonUtil.HashMapToJson(resultMap));
	}
	
	/* right/left outer join의 차이점은 기준이 되는 테이블이다
	 * left join의 기준은 from xxxx가 기준이고 right join은  right/left outer join xxxx이 기준이 된다.
	 * 
	 * inner join의 경우 join의 조건에 해당하는 열만 찾아내지만 
	 * outer join은 조건에 해당하지 않더라도 기준이 되는 테이블은 모두 찾아내고 join시키는 테이블은 조건에 따라 null로 채우는 식으로
	 * 테이블을 붙여서 찾아낸다.
	 * */

}
