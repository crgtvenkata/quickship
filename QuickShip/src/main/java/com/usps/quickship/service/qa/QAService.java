package com.usps.quickship.service.qa;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.usps.quickship.common.util.mapper.qa.AnswerMapper;
import com.usps.quickship.common.util.mapper.qa.CommentMapper;
import com.usps.quickship.common.util.mapper.qa.QuestionMapper;
import com.usps.quickship.data.model.qa.Answer;
import com.usps.quickship.data.model.qa.Bookmark;
import com.usps.quickship.data.model.qa.Comment;
import com.usps.quickship.data.model.qa.Question;
import com.usps.quickship.data.model.qa.Tag;
import com.usps.quickship.data.model.qa.Vote;
import com.usps.quickship.data.model.user.User;
import com.usps.quickship.data.repo.qa.QARepository;
import com.usps.quickship.data.repo.qa.TagRepository;
import com.usps.quickship.data.repo.user.UserRepository;
import com.usps.quickship.service.user.UserService;
import com.usps.quickship.web.model.qa.AnswerUI;
import com.usps.quickship.web.model.qa.CommentUI;
import com.usps.quickship.web.model.qa.QuestionUI;

@Service
public class QAService {

	@Autowired
	private QARepository qaRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private TagRepository tagRepo;
	@Autowired
	MongoTemplate template;

	private QuestionMapper qMapper = new QuestionMapper();
	private AnswerMapper aMapper = new AnswerMapper();
	private CommentMapper cMapper = new CommentMapper();
	private Logger logger = Logger.getLogger(UserService.class);

	public QuestionUI create(QuestionUI uiBean, Principal principal) {
		Question newQ = qMapper.toPersistenceBean(uiBean);

		User user = userRepo.findByUserName(principal.getName());
		if (user != null) {
			newQ.setCreatedBy(user);
		}
		newQ.setCreatedDate(Calendar.getInstance().getTime());

		newQ = qaRepo.save(newQ);
		addTags(newQ);

		logger.debug(newQ);
		return qMapper.toUIBean(newQ);
	}

	public List<QuestionUI> findAll() {
		return qMapper.toUIBean(qaRepo.findAll());
	}

	public Page<QuestionUI> findAll(Pageable pageable) {
		return qMapper.toUIBean(qaRepo.findAll(pageable), pageable);
	}

	public QuestionUI findBySubject(String subject) {
		return qMapper.toUIBean(qaRepo.findBySubject(subject));
	}

	public QuestionUI findById(String id) {
		return qMapper.toUIBean(qaRepo.findById(id));
	}

	public List<QuestionUI> findById(List<String> id) {
		List<Question> questions = new ArrayList<Question>();
		questions = template.find(new Query(Criteria.where("id").in(id)),
				Question.class);

		return qMapper.toUIBean(questions);
	}

	public QuestionUI findById(String id, Principal principal) {
		template.updateFirst(new Query(Criteria.where("id").is(id)),
				new Update().inc("viewCount", 1), Question.class);

		return qMapper.toUIBean(qaRepo.findById(id));
	}

	public QuestionUI update(QuestionUI question) {
		Question existing = qaRepo.findBySubject(question.getSubject());

		return qMapper.toUIBean(qaRepo.save(existing));
	}

	public Boolean delete(QuestionUI question) {
		Question existing = qaRepo.findBySubject(question.getSubject());

		if (existing == null) {
			return false;
		}

		qaRepo.delete(existing);
		return true;
	}

	public QuestionUI addAnswer(AnswerUI answer, Principal principal) {
		QuestionUI result = new QuestionUI();

		Answer newAnswer = aMapper.toPersistenceBean(answer);
		newAnswer.setId(UUID.randomUUID().toString());
		newAnswer.setCreatedDate(Calendar.getInstance().getTime());
		User user = userRepo.findByUserName(principal.getName());
		if (user != null) {
			newAnswer.setCreatedBy(user);
		}

		Query q = new Query(Criteria.where("id").is(answer.getQuestionId()));
		Update updateQ = new Update().set("answers." + newAnswer.getId(),
				newAnswer);
		template.updateFirst(q, updateQ, Question.class);

		result = qMapper.toUIBean(template.findOne(q, Question.class));
		return result;
	}

	public QuestionUI addQuestionComment(CommentUI comment, Principal principal) {
		QuestionUI result = new QuestionUI();
		Comment newComment = cMapper.toPersistenceBean(comment);
		newComment.setId(UUID.randomUUID().toString());
		newComment.setCreatedDate(Calendar.getInstance().getTime());
		User user = userRepo.findByUserName(principal.getName());
		if (user != null) {
			newComment.setCreatedBy(user);
		}

		Query q = new Query(Criteria.where("id").is(comment.getQuestionId()));
		Update updateQ = new Update().set("comments." + newComment.getId(),
				newComment);
		template.updateFirst(q, updateQ, Question.class);

		result = qMapper.toUIBean(template.findOne(q, Question.class));
		return result;
	}

	public QuestionUI addAnswerComment(CommentUI comment, Principal principal) {
		QuestionUI result = new QuestionUI();
		Comment newComment = cMapper.toPersistenceBean(comment);
		newComment.setId(UUID.randomUUID().toString());
		newComment.setCreatedDate(Calendar.getInstance().getTime());
		User user = userRepo.findByUserName(principal.getName());
		if (user != null) {
			newComment.setCreatedBy(user);
		}

		Query q = new Query(Criteria.where("id").is(comment.getQuestionId()));
		Update updateQ = new Update().set("answers." + comment.getAnswerId()
				+ ".comments." + newComment.getId(), newComment);
		template.updateFirst(q, updateQ, Question.class);

		result = qMapper.toUIBean(template.findOne(q, Question.class));
		return result;
	}

	public QuestionUI addVote(String questionId, Principal principal) {
		QuestionUI result = new QuestionUI();
		User user = userRepo.findByUserName(principal.getName());

		Query q = new Query(Criteria.where("id").is(questionId));
		if (user != null) {
			Vote vote = new Vote();
			vote.setId(user.getId());
			vote.setUser(user.getUserName());
			vote.setCreatedDate(Calendar.getInstance().getTime());

			Update updateQ = new Update().set("votes." + user.getId(), vote);
			template.updateFirst(q, updateQ, Question.class);
		}

		result = qMapper.toUIBean(template.findOne(q, Question.class));
		return result;
	}

	public QuestionUI addBookmark(String questionId, Principal principal) {
		QuestionUI result = new QuestionUI();
		User user = userRepo.findByUserName(principal.getName());

		Query q = new Query(Criteria.where("id").is(questionId));
		if (user != null) {
			Bookmark bookmark = new Bookmark();
			bookmark.setId(user.getId());
			bookmark.setUser(user.getUserName());
			bookmark.setQuestionId(questionId);
			bookmark.setCreatedDate(Calendar.getInstance().getTime());

			template.updateFirst(new Query(Criteria.where("id")
					.is(user.getId())), new Update().set("bookmarks."
					+ questionId, bookmark), User.class);

			Update updateQ = new Update().set("bookmarks." + user.getId(),
					bookmark);
			template.updateFirst(q, updateQ, Question.class);
		}

		result = qMapper.toUIBean(template.findOne(q, Question.class));
		return result;
	}

	private void addTags(Question question) {
		for (String tagName : question.getTags()) {
			Tag existingTag = tagRepo.findByName(tagName);
			if (existingTag != null) {
				Query q = new Query(Criteria.where("id")
						.is(existingTag.getId()));
				template.updateFirst(
						q,
						new Update().push("questions", question.getId()).inc(
								"questionCount", 1), Tag.class);
			} else {
				Tag newTag = new Tag();
				newTag.setName(tagName);
				newTag.setCreatedBy(question.getCreatedBy());
				newTag.setCreatedDate(Calendar.getInstance().getTime());
				newTag.setQuestionCount(1);

				List<String> questions = new ArrayList<String>();
				questions.add(question.getId());
				newTag.setQuestions(questions);

				template.insert(newTag);
			}
		}
	}
}