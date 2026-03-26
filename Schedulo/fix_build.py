import os
import re

root_dir = r"c:\Users\Nims-Creation\Downloads\Schedulo\Schedulo\src\main\java\com\saas\Schedulo"

for subdir, dirs, files in os.walk(root_dir):
    for file in files:
        if file.endswith(".java"):
            path = os.path.join(subdir, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()

            new_content = content
            
            # 1. Replace jackson
            new_content = new_content.replace('import tools.jackson.databind.ObjectMapper;', 'import com.fasterxml.jackson.databind.ObjectMapper;')
            
            # 2. Fix validation imports
            if '@NotNull' in new_content or '@NotBlank' in new_content or '@Min(' in new_content or '@Size(' in new_content or '@Positive' in new_content:
                new_content = new_content.replace('import org.antlr.v4.runtime.misc.NotNull;\n', '')
                new_content = new_content.replace('import org.antlr.v4.runtime.misc.NotNull;', '')
                if 'import jakarta.validation.constraints.*' not in new_content:
                    new_content = re.sub(r'(package [^;]+;)', r'\1\n\nimport jakarta.validation.constraints.*;', new_content, 1)
            
            # 3. Service imports
            if file == 'PaymentService.java':
                if 'import java.util.List;' not in new_content:
                    new_content = new_content.replace('public interface PaymentService', 'import java.util.List;\nimport com.saas.Schedulo.dto.response.payment.PaymentMethodResponse;\nimport com.saas.Schedulo.dto.response.payment.InvoiceResponse;\n\npublic interface PaymentService', 1)
            
            if file == 'SubscriptionService.java':
                if 'import java.util.List;' not in new_content:
                    new_content = new_content.replace('public interface SubscriptionService', 'import java.util.List;\nimport com.saas.Schedulo.dto.response.subscription.SubscriptionPlanResponse;\n\npublic interface SubscriptionService', 1)

            if file == 'CalendarService.java':
                if 'import com.saas.Schedulo.dto.response.calendar.HolidayResponse;' not in new_content:
                    new_content = new_content.replace('public interface CalendarService', 'import com.saas.Schedulo.dto.response.calendar.HolidayResponse;\n\npublic interface CalendarService', 1)

            # 4. MapStruct mapping errors (Unknown property id)
            if file in ['UserMapper.java', 'TimetableMapper.java']:
                if 'unmappedTargetPolicy' not in new_content:
                    new_content = new_content.replace('@Mapper(componentModel = "spring")', '@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)')

            if new_content != content:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Fixed {file}")
