from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define nodes
        user = Rectangle(width=2, height=1).shift(UP * 2).set_fill(BLUE, opacity=0.5)
        user_text = Text('User', font_size=24).move_to(user.get_center())
        user_group = Group(user, user_text)

        gemini = Rectangle(width=2, height=1).shift(UP * 0).set_fill(GREEN, opacity=0.5)
        gemini_text = Text('Gemini', font_size=24).move_to(gemini.get_center())
        gemini_group = Group(gemini, gemini_text)

        backend = Rectangle(width=2, height=1).shift(DOWN * 2).set_fill(YELLOW, opacity=0.5)
        backend_text = Text('Backend', font_size=24).move_to(backend.get_center())
        backend_group = Group(backend, backend_text)

        db = Rectangle(width=2, height=1).shift(DOWN * 4 + LEFT * 2).set_fill(RED, opacity=0.5)
        db_text = Text('Database', font_size=20).move_to(db.get_center())
        db_group = Group(db, db_text)

        redis = Rectangle(width=2, height=1).shift(DOWN * 4 + RIGHT * 2).set_fill(PURPLE, opacity=0.5)
        redis_text = Text('Redis', font_size=20).move_to(redis.get_center())
        redis_group = Group(redis, redis_text)

        # Define edges
        prompt_arrow = Arrow(user_group.get_edge(DOWN), gemini_group.get_edge(UP), buff=0.2)
        prompt_text = Text('Prompt', font_size=18).move_to(Line(user_group.get_edge(DOWN), gemini_group.get_edge(UP)).point_from_proportion(0.5) + LEFT*0.5)
        prompt_group = Group(prompt_arrow, prompt_text)

        response_arrow = Arrow(gemini_group.get_edge(DOWN), backend_group.get_edge(UP), buff=0.2)
        response_text = Text('Response Text', font_size=18).move_to(Line(gemini_group.get_edge(DOWN), backend_group.get_edge(UP)).point_from_proportion(0.5) + LEFT*0.5)
        response_group = Group(response_arrow, response_text)

        db_arrow = Arrow(backend_group.get_edge(DOWN + LEFT*0.5), db_group.get_edge(UP), buff=0.2)
        db_text = Text('Save to DB', font_size=18).move_to(Line(backend_group.get_edge(DOWN + LEFT*0.5), db_group.get_edge(UP)).point_from_proportion(0.5)+ LEFT*0.5)
        db_group_arrow = Group(db_arrow, db_text)

        redis_arrow = Arrow(backend_group.get_edge(DOWN + RIGHT*0.5), redis_group.get_edge(UP), buff=0.2)
        redis_text = Text('Save to Redis', font_size=18).move_to(Line(backend_group.get_edge(DOWN + RIGHT*0.5), redis_group.get_edge(UP)).point_from_proportion(0.5)+ RIGHT*0.5)
        redis_group_arrow = Group(redis_arrow, redis_text)

        # Animate
        self.play(Create(user_group), Create(gemini_group), Create(backend_group), Create(db_group), Create(redis_group))
        self.play(Create(prompt_group))
        self.play(Create(response_group))
        self.play(Create(db_group_arrow), Create(redis_group_arrow))
        self.wait(2)
