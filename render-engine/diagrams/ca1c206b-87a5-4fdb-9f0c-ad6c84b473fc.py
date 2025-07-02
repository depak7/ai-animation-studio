from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define nodes
        user = Rectangle(width=2, height=1, color=BLUE).shift(UP * 2.5).set_fill(BLUE, opacity=0.5)
        user_text = Text('User', font_size=24).move_to(user.get_center())
        user_group = Group(user, user_text)

        gemini = Rectangle(width=2, height=1, color=GREEN).shift(UP * 0.5).set_fill(GREEN, opacity=0.5)
        gemini_text = Text('Gemini', font_size=24).move_to(gemini.get_center())
        gemini_group = Group(gemini, gemini_text)

        backend = Rectangle(width=2.5, height=1, color=YELLOW).shift(DOWN * 1.5).set_fill(YELLOW, opacity=0.5)
        backend_text = Text('Backend', font_size=24).move_to(backend.get_center())
        backend_group = Group(backend, backend_text)

        db = Rectangle(width=2, height=1, color=RED).shift(DOWN * 3.5 + LEFT * 2)
        db_text = Text('Database', font_size=24).move_to(db.get_center())
        db_group = Group(db, db_text)

        redis = Rectangle(width=2, height=1, color=ORANGE).shift(DOWN * 3.5 + RIGHT * 2)
        redis_text = Text('Redis', font_size=24).move_to(redis.get_center())
        redis_group = Group(redis, redis_text)

        # Define edges
        prompt_arrow = Arrow(user_group.get_bottom(), gemini_group.get_top(), buff=0.5)
        prompt_text = Text('Prompt', font_size=20).move_to(prompt_arrow.get_center() + UP * 0.3)
        prompt_group = Group(prompt_arrow, prompt_text)

        response_arrow = Arrow(gemini_group.get_bottom(), backend_group.get_top(), buff=0.5)
        response_text = Text('Response', font_size=20).move_to(response_arrow.get_center() + UP * 0.3)
        response_group = Group(response_arrow, response_text)

        save_db_arrow = Arrow(backend_group.get_bottom(), db_group.get_top(), buff=0.5)
        save_db_text = Text('Save', font_size=20).move_to(save_db_arrow.get_center() + LEFT * 0.6 + DOWN * 0.2)
        save_db_group = Group(save_db_arrow, save_db_text)

        save_redis_arrow = Arrow(backend_group.get_bottom(), redis_group.get_top(), buff=0.5)
        save_redis_text = Text('Save (if success)', font_size=20).move_to(save_redis_arrow.get_center() + RIGHT * 0.6 + DOWN * 0.2)
        save_redis_group = Group(save_redis_arrow, save_redis_text)

        # Animate
        self.play(Create(user_group), Create(gemini_group), Create(backend_group), Create(db_group), Create(redis_group))
        self.play(Create(prompt_group))
        self.play(Create(response_group))
        self.play(Create(save_db_group), Create(save_redis_group))

        self.wait(3)
