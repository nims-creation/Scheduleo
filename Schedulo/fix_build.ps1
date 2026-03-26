$rootDir = "C:\Users\Nims-Creation\Downloads\Schedulo\Schedulo\src\main\java\com\saas\Schedulo"

Get-ChildItem -Path $rootDir -Recurse -Filter "*.java" | ForEach-Object {
    $path = $_.FullName
    $content = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    $newContent = $content

    # 1. Replace jackson
    $newContent = $newContent -replace 'import tools.jackson.databind.ObjectMapper;', 'import com.fasterxml.jackson.databind.ObjectMapper;'
    
    # 2. Fix validation imports
    if ($newContent -match '@NotNull' -or $newContent -match '@NotBlank' -or $newContent -match '@Min\(' -or $newContent -match '@Size\(' -or $newContent -match '@Positive') {
        $newContent = $newContent -replace 'import org.antlr.v4.runtime.misc.NotNull;\r?\n', ''
        $newContent = $newContent -replace 'import org.antlr.v4.runtime.misc.NotNull;', ''
        if ($newContent -notmatch 'import jakarta.validation.constraints.\*;') {
            $newContent = $newContent -replace '(?m)^(package [^;]+;)', "`$1`r`n`r`nimport jakarta.validation.constraints.*;"
        }
    }
    
    # 3. Service imports
    if ($_.Name -eq 'PaymentService.java') {
        if ($newContent -notmatch 'import java.util.List;') {
            $newContent = $newContent -replace 'public interface PaymentService', "import java.util.List;`r`nimport com.saas.Schedulo.dto.response.payment.PaymentMethodResponse;`r`nimport com.saas.Schedulo.dto.response.payment.InvoiceResponse;`r`n`r`npublic interface PaymentService"
        }
    }
    if ($_.Name -eq 'SubscriptionService.java') {
        if ($newContent -notmatch 'import java.util.List;') {
            $newContent = $newContent -replace 'public interface SubscriptionService', "import java.util.List;`r`nimport com.saas.Schedulo.dto.response.subscription.SubscriptionPlanResponse;`r`n`r`npublic interface SubscriptionService"
        }
    }
    if ($_.Name -eq 'CalendarService.java') {
        if ($newContent -notmatch 'import com.saas.Schedulo.dto.response.calendar.HolidayResponse;') {
            $newContent = $newContent -replace 'public interface CalendarService', "import com.saas.Schedulo.dto.response.calendar.HolidayResponse;`r`n`r`npublic interface CalendarService"
        }
    }

    # 4. MapStruct mapping errors
    if ($_.Name -eq 'UserMapper.java' -or $_.Name -eq 'TimetableMapper.java') {
        if ($newContent -notmatch 'unmappedTargetPolicy') {
            $newContent = $newContent -replace '@Mapper\(componentModel = "spring"\)', '@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)'
        }
    }

    if ($newContent -cne $content) {
        [System.IO.File]::WriteAllText($path, $newContent, [System.Text.Encoding]::UTF8)
        Write-Host "Fixed $($_.Name)"
    }
}
